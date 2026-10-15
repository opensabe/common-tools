package io.github.opensabe.common.testcontainers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RocketMQ namesrv+broker in one container for integration tests.
 * <p>
 * On Linux (GitHub Actions) uses host network so clients talk to fixed 9876/10911
 * without Docker port remapping. Remapping {@code listenPort} to a random host port
 * makes VIP ({@code listenPort - 2}) collide with the namesrv mapping and breaks consume
 * even when produce still returns SEND_OK.
 * <p>
 * On non-Linux, binds coordinated host ports so VIP ({@code brokerPort - 2}) stays valid.
 */
public class CustomizedRocketMQContainer extends GenericContainer<CustomizedRocketMQContainer> {
    private static final int defaultBrokerPermission = 6;
    public static final int NAMESRV_PORT = 9876;
    public static final int BROKER_PORT = 10911;
    public static final int BROKER_VIP_PORT = BROKER_PORT - 2;

    private final boolean hostNetwork;
    private final int namesrvHostPort;
    private final int brokerHostPort;

    public CustomizedRocketMQContainer() {
        super("apache/rocketmq:5.2.0");
        this.hostNetwork = isLinux();
        if (hostNetwork) {
            this.namesrvHostPort = NAMESRV_PORT;
            this.brokerHostPort = BROKER_PORT;
            withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    HostConfig.newHostConfig().withNetworkMode("host")));
        } else {
            HostPorts ports = HostPorts.allocate();
            this.namesrvHostPort = ports.namesrv;
            this.brokerHostPort = ports.broker;
            addFixedExposedPort(namesrvHostPort, NAMESRV_PORT);
            addFixedExposedPort(brokerHostPort, BROKER_PORT);
            addFixedExposedPort(brokerHostPort - 2, BROKER_VIP_PORT);
        }
    }

    private static boolean isLinux() {
        String os = System.getProperty("os.name", "");
        return os.toLowerCase().contains("linux");
    }

    @Override
    protected void configure() {
        String command = "#!/bin/bash\n"
                + "./mqnamesrv &\n"
                + "./mqbroker -n localhost:" + NAMESRV_PORT
                + " autoCreateTopicEnable=true autoCreateSubscriptionGroup=true\n";
        withCommand("sh", "-c", command);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        List<String> updateBrokerConfigCommands = new ArrayList<>();
        // Advertise an address clients on the test JVM can reach.
        updateBrokerConfigCommands.add(updateBrokerConfig("brokerIP1", clientHost()));
        if (!hostNetwork) {
            // Remap advertised listen port to the fixed host binding.
            updateBrokerConfigCommands.add(updateBrokerConfig("listenPort", brokerHostPort));
        }
        // Avoid VIP (listenPort-2) which is easy to mis-map under Docker port publishing.
        updateBrokerConfigCommands.add(updateBrokerConfig("vipChannelEnabled", "false"));
        updateBrokerConfigCommands.add(updateBrokerConfig("brokerPermission", defaultBrokerPermission));

        final String command = String.join(" && ", updateBrokerConfigCommands);
        ExecResult result = null;
        while (
                result == null
                        || result.getExitCode() != 0
                        || result.getStderr().contains("failed")
        ) {
            result = execInContainer("/bin/sh", "-c", command);
            System.out.println(result.getStdout());
            System.out.println(result.getStderr());
            Thread.sleep(1000);
        }

        // Best-effort topic create so consumers have route info before the first send.
        try {
            ExecResult topicResult = execInContainer(
                    "/bin/sh",
                    "-c",
                    "./mqadmin updateTopic -n localhost:" + NAMESRV_PORT
                            + " -t rocketmq-test-topic -c DefaultCluster"
            );
            System.out.println(topicResult.getStdout());
            System.out.println(topicResult.getStderr());
        } catch (Throwable t) {
            System.out.println("updateTopic skipped: " + t.getMessage());
        }

        waitForPortOpen(clientHost(), getNamesrvPort(), 60_000);
        waitForPortOpen(clientHost(), brokerHostPort, 60_000);
        System.out.println("RocketMQ namesrv ready at " + clientHost() + ":" + getNamesrvPort()
                + ", broker advertised " + clientHost() + ":" + brokerHostPort
                + (hostNetwork ? " (host network)" : " (fixed ports)"));
    }

    private String clientHost() {
        return hostNetwork ? "127.0.0.1" : getHost();
    }

    private String updateBrokerConfig(final String key, final Object val) {
        final String brokerAddr = "localhost:" + BROKER_PORT;
        return "./mqadmin updateBrokerConfig -b " + brokerAddr + " -k " + key + " -v " + val;
    }

    public int getNamesrvPort() {
        return namesrvHostPort;
    }

    @SneakyThrows
    private static void waitForPortOpen(String host, int port, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 1000);
                return;
            } catch (IOException ignored) {
                TimeUnit.MILLISECONDS.sleep(200);
            }
        }
        throw new IllegalStateException("Timed out waiting for " + host + ":" + port);
    }

    /**
     * Host ports where {@code brokerVip = broker - 2} matches RocketMQ VIP convention.
     */
    private static final class HostPorts {
        final int namesrv;
        final int broker;

        private HostPorts(int namesrv, int broker) {
            this.namesrv = namesrv;
            this.broker = broker;
        }

        static HostPorts allocate() {
            for (int attempt = 0; attempt < 200; attempt++) {
                int broker;
                try (ServerSocket socket = new ServerSocket(0)) {
                    broker = socket.getLocalPort();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to probe free broker port", e);
                }
                if (broker <= 1026) {
                    continue;
                }
                int vip = broker - 2;
                int namesrv;
                try (ServerSocket socket = new ServerSocket(0)) {
                    namesrv = socket.getLocalPort();
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to probe free namesrv port", e);
                }
                if (namesrv == broker || namesrv == vip) {
                    continue;
                }
                if (isFree(namesrv) && isFree(broker) && isFree(vip)) {
                    return new HostPorts(namesrv, broker);
                }
            }
            throw new IllegalStateException("Unable to allocate RocketMQ host ports");
        }

        private static boolean isFree(int port) {
            try (ServerSocket socket = new ServerSocket(port)) {
                socket.setReuseAddress(true);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
