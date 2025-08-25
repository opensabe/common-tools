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
package io.github.opensabe.spring.cloud.parent.common.test.log4j2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.github.opensabe.spring.cloud.parent.common.config.Log4j2Configuration;

@SpringJUnitConfig
@AutoConfigureObservability
@SpringBootTest(
        properties = {
                "eureka.client.enabled=false",
                "management.endpoints.web.exposure.include=*"
        },
        classes = Log4j2PrometheusMetricsTest.Main.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class Log4j2PrometheusMetricsTest {
    private final TestRestTemplate testRestTemplate;

    @Autowired
    public Log4j2PrometheusMetricsTest(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
    }

    @Test
    public void testMetricsEndpoint() {
        // 这里可以添加对 /actuator/prometheus 端点的测试
        String response = testRestTemplate.getForObject("/actuator/prometheus", String.class);
        // 验证响应内容是否包含预期的指标
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Log4j2Configuration.GAUGE_NAME_SUFFIX));
    }

    @SpringBootApplication
    public static class Main {
    }
}
