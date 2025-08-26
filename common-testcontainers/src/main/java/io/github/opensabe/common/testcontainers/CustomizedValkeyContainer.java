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

import org.testcontainers.containers.GenericContainer;

import com.github.dockerjava.api.command.InspectContainerResponse;

import lombok.SneakyThrows;

/**
 * 使用Valkey来做缓存服务
 * 由于初始化容器的时候还没有初始化日志框架，这里只能通过 System.out.println 打印日志
 */
public class CustomizedValkeyContainer extends GenericContainer<CustomizedValkeyContainer> {
    public static final int VALKEY_PORT = 6379;

    public CustomizedValkeyContainer() {
        super("valkey/valkey");
    }

    @Override
    @SneakyThrows
    protected void configure() {
        withExposedPorts(VALKEY_PORT);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        execInContainer("valkey-server");
        ExecResult result = null;
        while (
                result == null
                        || result.getExitCode() != 0
        ) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("executing command to check if valkey is started");
            result = execInContainer("valkey-cli", "ping");
            System.out.println("stdout: " + result.getStdout());
            System.out.println("stderr: " + result.getStderr());
        }
    }

    @Override
    public void start() {
        super.start();
        System.out.println("Valkey started at port: " + getRedisPort());
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("Valkey stopped");
    }

    public int getRedisPort() {
        return getMappedPort(VALKEY_PORT);
    }
}
