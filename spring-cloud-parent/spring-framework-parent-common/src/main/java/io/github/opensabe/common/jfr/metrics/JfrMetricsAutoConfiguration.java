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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import io.micrometer.core.instrument.MeterRegistry;

@AutoConfiguration
@EnableConfigurationProperties(JfrMetricsProperties.class)
public class JfrMetricsAutoConfiguration {

    /**
     * Do not use MeterFilter.maximumAllowableTags here: removed Top-N gauges would still
     * permanently deny those tag values. Cardinality is bounded by publisher Top-N removal.
     */
    @Bean
    @ConditionalOnProperty(prefix = "opensabe.jfr.metrics", name = "enabled", havingValue = "true")
    public JfrMetricsConsumer jfrMetricsConsumer(JfrMetricsProperties properties, MeterRegistry meterRegistry) {
        return new JfrMetricsConsumer(properties, meterRegistry);
    }
}
