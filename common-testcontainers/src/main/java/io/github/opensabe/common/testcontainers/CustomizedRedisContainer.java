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
 * 由于初始化容器的时候还没有初始化日志框架，这里只能通过 System.out.println 打印日志
 */
public class CustomizedRedisContainer extends GenericContainer<CustomizedRedisContainer> {
    public static final int REDIS_PORT = 6379;

    public CustomizedRedisContainer() {
        super("redis");
    }

    @Override
    @SneakyThrows
    protected void configure() {
        withExposedPorts(REDIS_PORT);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        execInContainer("redis-server");
        ExecResult result = null;
        while (
                result == null
                        || result.getExitCode() != 0
        ) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("executing command to check if redis is started");
            result = execInContainer("redis-cli", "ping");
            System.out.println("stdout: " + result.getStdout());
            System.out.println("stderr: " + result.getStderr());
        }
    }

    @Override
    public void start() {
        super.start();
        System.out.println("Redis started at port: " + getRedisPort());
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("Redis stopped");
    }

    public int getRedisPort() {
        return getMappedPort(REDIS_PORT);
    }
}
