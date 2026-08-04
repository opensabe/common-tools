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
package io.github.opensabe.common.config;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验公用环境默认固定忽略 Observation LongTaskTimer，且运行时确实不创建 {@code *.active}。
 */
@SpringBootTest(classes = OpensabeMetricsEnvironmentPostProcessorTest.App.class)
@DisplayName("OpensabeMetricsEnvironmentPostProcessor：固定忽略 LongTaskTimer")
class OpensabeMetricsEnvironmentPostProcessorTest {

    private static final String PROBE_NAME = "opensabe.ltt.probe";

    private final OpensabeMetricsEnvironmentPostProcessor processor = new OpensabeMetricsEnvironmentPostProcessor();

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Autowired
    private MetricsProperties metricsProperties;

    @Test
    @DisplayName("未配置时写入 ignored-meters=long_task_timer")
    void addsDefaultWhenAbsent() {
        StandardEnvironment environment = new StandardEnvironment();
        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals(
                OpensabeMetricsEnvironmentPostProcessor.LONG_TASK_TIMER,
                environment.getProperty(OpensabeMetricsEnvironmentPostProcessor.IGNORED_METERS_PROPERTY));
        assertTrue(environment.getPropertySources().contains(
                OpensabeMetricsEnvironmentPostProcessor.PROPERTY_SOURCE_NAME));
    }

    @Test
    @DisplayName("应用已显式配置时不覆盖")
    void doesNotOverrideExplicitConfiguration() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource(
                "app",
                Map.of(OpensabeMetricsEnvironmentPostProcessor.IGNORED_METERS_PROPERTY, "")));
        processor.postProcessEnvironment(environment, new SpringApplication());

        assertEquals("", environment.getProperty(OpensabeMetricsEnvironmentPostProcessor.IGNORED_METERS_PROPERTY));
        assertFalse(environment.getPropertySources().contains(
                OpensabeMetricsEnvironmentPostProcessor.PROPERTY_SOURCE_NAME));
    }

    @Test
    @DisplayName("Boot MetricsProperties 已绑定 IgnoredMeters.LONG_TASK_TIMER")
    void metricsPropertiesIgnoresLongTaskTimer() {
        Set<DefaultMeterObservationHandler.IgnoredMeters> ignored =
                metricsProperties.getObservations().getIgnoredMeters();
        assertTrue(
                ignored.contains(DefaultMeterObservationHandler.IgnoredMeters.LONG_TASK_TIMER),
                () -> "expected LONG_TASK_TIMER in ignored-meters, but was: " + ignored);
    }

    @Test
    @DisplayName("Observation 产生 Timer，但不产生 *.active LongTaskTimer")
    void observationDoesNotRegisterLongTaskTimer() {
        Observation.start(PROBE_NAME, observationRegistry).stop();

        assertNotNull(
                meterRegistry.find(PROBE_NAME).timer(),
                "Timer for the observation name must still be recorded");
        assertNull(
                meterRegistry.find(PROBE_NAME + ".active").longTaskTimer(),
                "LongTaskTimer (*.active) must not be created when ignored-meters includes long_task_timer");
    }

    /**
     * 最小 Spring Boot 应用，加载本模块 AutoConfiguration 与 Boot Metrics。
     */
    @SpringBootApplication
    static class App {
    }
}
