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

import io.github.opensabe.common.testcontainers.CustomizedDynamoDBContainer;
import lombok.extern.log4j.Log4j2;

/**
 * 注意使用这个类的单元测试，用的是同一个 dynamodb，不同单元测试注意隔离不同的 key
 */
@Log4j2
public class SingleDynamoDbIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    public static CustomizedDynamoDBContainer DYNAMO_DB = new CustomizedDynamoDBContainer();

    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("aws_access_key_id", () -> "fake");
        registry.add("aws_secret_access_key", () -> "fake");
        registry.add("aws_env", () -> "test");
        registry.add("dynamolLocalUrl", () -> "http://localhost:" + DYNAMO_DB.getDynamoDBPort());
        registry.add("defaultOperId", () -> 0);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        //由于单元测试并发执行，这个只能启动一次，所以加锁
        if (!DYNAMO_DB.isRunning()) {
            synchronized (SingleDynamoDbIntegrationTest.class) {
                if (!DYNAMO_DB.isRunning()) {
                    DYNAMO_DB.start();
                }
            }
        }
    }

    @Override
    public void close() throws Throwable {
        DYNAMO_DB.stop();
    }
}
