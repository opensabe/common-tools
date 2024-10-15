package io.github.opensabe.common.testcontainers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;

public class CustomizedRocketMQContainer extends GenericContainer<CustomizedRocketMQContainer> {
    // READ and WRITE
    private static final int defaultBrokerPermission = 6;
    public static final int NAMESRV_PORT = 9876;
    public static final int BROKER_PORT = 10911;

    public CustomizedRocketMQContainer() {
        super("dyrnq/rocketmq:5.2.0");
        withExposedPorts(NAMESRV_PORT, BROKER_PORT, BROKER_PORT - 2);
    }

    @Override
    protected void configure() {
        String command = "#!/bin/bash\n";
        command += "./mqnamesrv &\n";
        command += "./mqbroker -n localhost:" + NAMESRV_PORT;
        withCommand("sh", "-c", command);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        List<String> updateBrokerConfigCommands = new ArrayList<>();
        // Update the brokerAddr and the clients can use the mapped address to connect the broker.
        updateBrokerConfigCommands.add(updateBrokerConfig("brokerIP1", getHost()));
        // Make the changes take effect immediately.
        updateBrokerConfigCommands.add(updateBrokerConfig("brokerPermission", defaultBrokerPermission));
        updateBrokerConfigCommands.add(updateBrokerConfig("listenPort", getMappedPort(BROKER_PORT)));

        final String command = String.join(" && ", updateBrokerConfigCommands);
        ExecResult result = null;
        //直到执行成功
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
        result = execInContainer(
                "/bin/sh",
                "-c",
                "./mqadmin clusterList -n localhost:" + NAMESRV_PORT
        );
        System.out.println(result.getStdout());
        System.out.println(result.getStderr());
    }

    private String updateBrokerConfig(final String key, final Object val) {
        final String brokerAddr = "localhost:" + BROKER_PORT;
        return "./mqadmin updateBrokerConfig -b " + brokerAddr + " -k " + key + " -v " + val;
    }

    public int getNamesrvPort() {
        return getMappedPort(NAMESRV_PORT);
    }
}