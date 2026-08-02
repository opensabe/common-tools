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
package io.github.opensabe.common.redisson.test.common;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;

import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;

/**
 * Redisson Starter 集成测试基类：SingleRedis Testcontainers、Tracing 与 JFR 支持。
 */
@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@SpringBootTest(
        classes = BaseRedissonTest.App.class,
        properties = {
                "eureka.client.enabled=false",
                "management.tracing.sampling.probability=1.0",
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTracing
@JfrEventTest
@DisplayName("Redisson Starter 集成测试基类")
public abstract class BaseRedissonTest {
    public static final int AOP_ORDER = 10000;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
        registry.add("spring.redis.redisson.aop.order", () -> AOP_ORDER);
    }

    @SpringBootApplication
    public static class App {
    }
}
