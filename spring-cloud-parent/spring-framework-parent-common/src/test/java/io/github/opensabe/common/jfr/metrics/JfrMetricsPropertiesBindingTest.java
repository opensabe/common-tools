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
package io.github.opensabe.common.jfr.metrics;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.util.unit.DataSize;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JfrMetricsPropertiesBindingTest {

    @Test
    void defaultsStayDisabled() {
        JfrMetricsProperties properties = new JfrMetricsProperties();
        assertFalse(properties.isEnabled());
        assertEquals(30, properties.getPublish().getTopN());
        assertEquals(Duration.ofSeconds(15), properties.getPublish().getFlushInterval());
        assertTrue(properties.getConsume().getAllocationSample().isEnabled());
        assertEquals(DataSize.ofKilobytes(64), properties.getConsume().getOutsideTlab().getMinSize());
    }

    @Test
    void bindsNestedConsumeAndCircuitBreaker() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "opensabe.jfr.metrics.enabled", "true",
                "opensabe.jfr.metrics.publish.top-n", "15",
                "opensabe.jfr.metrics.circuit-breaker.max-per-second", "100",
                "opensabe.jfr.metrics.consume.outside-tlab.min-size", "128KB",
                "opensabe.jfr.metrics.consume.execution-sample.enabled", "false"
        ));
        JfrMetricsProperties properties = new Binder(source)
                .bind("opensabe.jfr.metrics", Bindable.of(JfrMetricsProperties.class))
                .get();
        assertTrue(properties.isEnabled());
        assertEquals(15, properties.getPublish().getTopN());
        assertEquals(100, properties.getCircuitBreaker().getMaxPerSecond());
        assertEquals(DataSize.ofKilobytes(128), properties.getConsume().getOutsideTlab().getMinSize());
        assertFalse(properties.getConsume().getExecutionSample().isEnabled());
    }
}
