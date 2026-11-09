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
import io.micrometer.core.instrument.config.MeterFilter;

@AutoConfiguration
@EnableConfigurationProperties(JfrMetricsProperties.class)
public class JfrMetricsAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "opensabe.jfr.metrics", name = "enabled", havingValue = "true")
    public JfrMetricsConsumer jfrMetricsConsumer(JfrMetricsProperties properties, MeterRegistry meterRegistry) {
        int topN = Math.max(1, properties.getPublish().getTopN());
        int maxTags = Math.max(topN + 10, 50);
        meterRegistry.config()
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.ALLOC_SAMPLE_BY_CLASS, "class", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.ALLOC_SAMPLE_BY_METHOD, "method", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.OUTSIDE_TLAB_BY_CLASS, "class", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.ALLOC_REQ_GC_BY_METHOD, "method", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.ALLOC_REQ_GC_MAX_BY_METHOD, "method", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.OLD_OBJECT_SAMPLES, "class", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.EXECUTION_SAMPLES, "method", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.MONITOR_WAIT, "method", maxTags, MeterFilter.deny()))
                .meterFilter(MeterFilter.maximumAllowableTags(
                        JfrMetricsPublisher.MONITOR_COUNT, "method", maxTags, MeterFilter.deny()));
        return new JfrMetricsConsumer(properties, meterRegistry);
    }
}
