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
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consume-only JFR → Micrometer settings. Never starts or reconfigures JVM Flight Recorder.
 * Default {@code enabled=false} keeps upgrades no-op (zero consumer overhead).
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "opensabe.jfr.metrics")
public class JfrMetricsProperties {

    /**
     * Master switch. When false, no EventStream is opened and no consumer threads start.
     * Does not stop process-side JFR recording.
     */
    private boolean enabled = false;

    private Publish publish = new Publish();

    private Stack stack = new Stack();

    private Repository repository = new Repository();

    private CircuitBreaker circuitBreaker = new CircuitBreaker();

    private Consume consume = new Consume();

    @Data
    @NoArgsConstructor
    public static class Publish {
        /**
         * Max label values kept per metric after each flush window.
         */
        private int topN = 30;

        /**
         * How often window aggregates are published as Gauges.
         */
        private Duration flushInterval = Duration.ofSeconds(15);
    }

    @Data
    @NoArgsConstructor
    public static class Stack {
        /**
         * Preferred packages when picking a business frame. Empty = skip java/jdk/sun/reactor.
         */
        private List<String> businessPackages = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    public static class Repository {
        /**
         * Max time waiting for {@code jdk.jfr.repository} (disk recording) before giving up a cycle.
         */
        private Duration attachTimeout = Duration.ofSeconds(30);

        /**
         * Retry interval when no disk repository is present yet.
         */
        private Duration retryInterval = Duration.ofSeconds(5);
    }

    @Data
    @NoArgsConstructor
    public static class CircuitBreaker {
        /**
         * Per-event consumer rate cap. Over this, events are dropped without allocating keys;
         * sustained breach unsubscribes the handler (does not disable JVM recording).
         */
        private int maxPerSecond = 500;

        /**
         * All events combined. Sustained breach closes the whole EventStream temporarily.
         */
        private int globalMaxPerSecond = 2000;

        /**
         * Wait before re-subscribing after a trip.
         */
        private Duration cooldown = Duration.ofMinutes(1);
    }

    @Data
    @NoArgsConstructor
    public static class Consume {
        /**
         * false = do not subscribe; does not change process JFR settings.
         */
        private EventToggle allocationSample = enabledEvent();

        private OutsideTlab outsideTlab = new OutsideTlab();

        private EventToggle allocationRequiringGc = enabledEvent();

        private EventToggle oldObjects = enabledEvent();

        private EventToggle executionSample = enabledEvent();

        private EventToggle monitorEnter = enabledEvent();

        private EventToggle zgcAllocationStall = enabledEvent();
    }

    @Data
    @NoArgsConstructor
    public static class EventToggle {
        private boolean enabled = true;
    }

    @Data
    @NoArgsConstructor
    public static class OutsideTlab {
        private boolean enabled = true;

        /**
         * Aggregation filter only; does not reduce JVM recording cost if the event is enabled in jfc.
         */
        private DataSize minSize = DataSize.ofKilobytes(64);
    }

    private static EventToggle enabledEvent() {
        EventToggle toggle = new EventToggle();
        toggle.setEnabled(true);
        return toggle;
    }
}
