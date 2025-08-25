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
package io.github.opensabe.common.testcontainers.integration;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;

import io.github.opensabe.common.testcontainers.CustomizedHttpBinContainer;
import lombok.extern.log4j.Log4j2;

/**
 * 注册一个微服务
 */
@Log4j2
public class SingleHttpBinMicroServiceIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static final String HTTP_BIN_SERVICE_NAME = "httpbin";
    public static final String HTTP_BIN_SERVICE_ZONE = "test";
    public static final String HTTP_BIN_SERVICE_INSTANCE_ID = "test-httpbin-1";
    public static CustomizedHttpBinContainer HTTP_BIN = new CustomizedHttpBinContainer();

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.cloud.discovery.client.simple.instances." + HTTP_BIN_SERVICE_NAME + "[0].uri",
                () -> "http://localhost:" + HTTP_BIN.getHttpBinPort()
        );
        registry.add("spring.cloud.discovery.client.simple.instances.httpbin[0].metadata.zone", () -> {
            return HTTP_BIN_SERVICE_ZONE;
        });
        registry.add("spring.cloud.discovery.client.simple.instances.httpbin[0].instance-id", () -> {
            return HTTP_BIN_SERVICE_INSTANCE_ID;
        });
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!HTTP_BIN.isRunning()) {
            synchronized (SingleHttpBinMicroServiceIntegrationTest.class) {
                if (!HTTP_BIN.isRunning()) {
                    HTTP_BIN.start();
                }
            }
        }
    }

    @Override
    public void close() throws Throwable {
        HTTP_BIN.stop();
    }
}
