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

import com.github.dockerjava.api.command.InspectContainerResponse;
import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

public class CustomizedHttpBinContainer extends GenericContainer<CustomizedHttpBinContainer> {

    public static final int HTTP_BIN_PORT = 8080;

    public CustomizedHttpBinContainer() {
        super("mccutchen/go-httpbin");
    }

    @Override
    protected void configure() {
        withExposedPorts(HTTP_BIN_PORT);
    }

    @Override
    @SneakyThrows
    protected void containerIsStarted(InspectContainerResponse containerInfo) {
        System.out.println("HTTP_BIN started at port: " + getHttpBinPort());
    }

    @Override
    public void stop() {
        super.stop();
        System.out.println("HTTP_BIN stopped");
    }

    public int getHttpBinPort() {
        return getMappedPort(HTTP_BIN_PORT);
    }
}
