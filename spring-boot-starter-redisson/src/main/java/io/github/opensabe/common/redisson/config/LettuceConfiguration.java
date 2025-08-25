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
package io.github.opensabe.common.redisson.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.lettuce.core.event.DefaultEventPublisherOptions;
import io.lettuce.core.metrics.DefaultCommandLatencyCollector;
import io.lettuce.core.metrics.DefaultCommandLatencyCollectorOptions;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;

@Configuration(proxyBeanMethods = false)
public class LettuceConfiguration {
    private static final String DEFAULT_APPLICATION_NAME = "application";

    @Bean
    public ClientResourcesBuilderCustomizer clientResourcesBuilderCustomizer(ObservationRegistry observationRegistry, Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", DEFAULT_APPLICATION_NAME);
        return builder -> builder
                .tracing(new MicrometerTracing(observationRegistry, applicationName))
                .commandLatencyRecorder(new DefaultCommandLatencyCollector(DefaultCommandLatencyCollectorOptions.builder().enable().resetLatenciesAfterEvent(true).build()))
                .commandLatencyPublisherOptions(DefaultEventPublisherOptions.builder().eventEmitInterval(Duration.ofSeconds(10)).build())
                ;
    }

}
