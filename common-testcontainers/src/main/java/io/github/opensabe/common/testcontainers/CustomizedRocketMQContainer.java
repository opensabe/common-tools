/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.testcontainers;

import java.util.ArrayList;
import java.util.List;

import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.command.InspectContainerResponse;

import lombok.SneakyThrows;

public class CustomizedRocketMQContainer extends GenericContainer<CustomizedRocketMQContainer> {
    public static final int NAMESRV_PORT = 9876;
    public static final int BROKER_PORT = 10911;
    // READ and WRITE
    private static final int DEFAULT_BROKER_PERMISSION = 6;

    public CustomizedRocketMQContainer() {
        super("dyrnq/rocketmq:5.3.2");
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
        List<String> updateBrokerConfigCommands = new ArrayList<>(4);
        // Update the brokerAddr and the clients can use the mapped address to connect the broker.
        updateBrokerConfigCommands.add(updateBrokerConfig("brokerIP1", getHost()));
        // Make the changes take effect immediately.
        updateBrokerConfigCommands.add(updateBrokerConfig("listenPort", getMappedPort(BROKER_PORT)));
        updateBrokerConfigCommands.add(updateBrokerConfig("brokerPermission", DEFAULT_BROKER_PERMISSION));
        updateBrokerConfigCommands.add(updateBrokerConfig("namesrvAddr", "localhost:" + NAMESRV_PORT));

        final String command = String.join(" && ", updateBrokerConfigCommands);
        ExecResult result = null;
        //直到执行成功
        while (
                result == null
                        || result.getExitCode() != 0
                        || result.getStderr().contains("failed")
        ) {
            System.out.println("---------".repeat(10));
            System.out.println(command);
            result = execInContainer("/bin/sh", "-c", command);
            System.out.println(result.getStdout());
            System.out.println(result.getStderr());
//            Thread.sleep(2000);
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