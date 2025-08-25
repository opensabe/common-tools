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

import java.util.concurrent.TimeUnit;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.command.InspectContainerResponse;

import lombok.SneakyThrows;

public class CustomizedDynamoDBContainer extends GenericContainer<CustomizedDynamoDBContainer> {
    public static final int DYNAMODB_PORT = 8000;

    public CustomizedDynamoDBContainer() {
        super("amazon/dynamodb-local");
    }

    @Override
    protected void configure() {
        withExposedPorts(DYNAMODB_PORT);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        Container.ExecResult execResult = null;
        //直到执行成功
        while (execResult == null || execResult.getExitCode() != 0) {
            TimeUnit.SECONDS.sleep(1);
            execResult = execInContainer("curl", "http://localhost:" + DYNAMODB_PORT);
            System.out.println("stdout: " + execResult.getStdout());
            System.out.println("stderr: " + execResult.getStderr());
        }
        System.out.println("DynamoDB started at port: " + getDynamoDBPort());
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("DynamoDB stopped");
    }

    public int getDynamoDBPort() {
        return getMappedPort(DYNAMODB_PORT);
    }
}
