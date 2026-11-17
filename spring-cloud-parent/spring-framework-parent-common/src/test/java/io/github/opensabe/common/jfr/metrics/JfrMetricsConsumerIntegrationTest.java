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

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full attach test needs a disk JFR repository
 * ({@code -XX:StartFlightRecording=disk=true,name=test,settings=default}).
 */
class JfrMetricsConsumerIntegrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JfrMetricsAutoConfiguration.class))
            .withBean(SimpleMeterRegistry.class)
            .withPropertyValues(
                    "opensabe.jfr.metrics.enabled=true",
                    "opensabe.jfr.metrics.publish.flush-interval=1s",
                    "opensabe.jfr.metrics.repository.attach-timeout=5s",
                    "opensabe.jfr.metrics.repository.retry-interval=500ms"
            );

    @Test
    void disabledMeansNoConsumerBean() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JfrMetricsAutoConfiguration.class))
                .withBean(SimpleMeterRegistry.class)
                .withPropertyValues("opensabe.jfr.metrics.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(JfrMetricsConsumer.class));
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void enabledAttachesWhenDiskRecordingPresent() throws InterruptedException {
        String repository = System.getProperty("jdk.jfr.repository");
        org.junit.jupiter.api.Assumptions.assumeTrue(
                repository != null && !repository.isBlank(),
                "JDK disk JFR repository not present; start test JVM with -XX:StartFlightRecording");

        runner.run(context -> {
            assertThat(context).hasSingleBean(JfrMetricsConsumer.class);
            SimpleMeterRegistry registry = context.getBean(SimpleMeterRegistry.class);
            byte[] sink = null;
            for (int i = 0; i < 20_000; i++) {
                sink = new byte[2048];
            }
            assertThat(sink).isNotNull();

            boolean attached = false;
            for (int i = 0; i < 40; i++) {
                Gauge gauge = registry.find("jfr_metrics_repository_attached").gauge();
                if (gauge != null && gauge.value() == 1.0d) {
                    attached = true;
                    break;
                }
                try {
                    Thread.sleep(250L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            assertThat(attached).as("repository should attach").isTrue();
        });
    }
}
