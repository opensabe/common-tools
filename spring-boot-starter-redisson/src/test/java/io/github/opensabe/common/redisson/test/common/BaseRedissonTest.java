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
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@AutoConfigureObservability
@SpringBootTest(
        classes = BaseRedissonTest.App.class,
        properties = {
                "eureka.client.enabled=false",
        }
)
@JfrEventTest
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
