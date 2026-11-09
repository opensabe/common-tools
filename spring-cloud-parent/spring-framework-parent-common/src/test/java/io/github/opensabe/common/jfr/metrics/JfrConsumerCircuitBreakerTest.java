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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JfrConsumerCircuitBreakerTest {

    @Test
    void tripsEventWhenOverPerSecondLimit() {
        JfrMetricsProperties.CircuitBreaker props = new JfrMetricsProperties.CircuitBreaker();
        props.setMaxPerSecond(10);
        props.setGlobalMaxPerSecond(10_000);
        props.setCooldown(Duration.ofSeconds(1));
        JfrConsumerCircuitBreaker breaker = new JfrConsumerCircuitBreaker(props);
        for (int i = 0; i < 10; i++) {
            assertTrue(breaker.tryAcquire("jdk.ObjectAllocationSample"));
        }
        assertFalse(breaker.tryAcquire("jdk.ObjectAllocationSample"));
        assertTrue(breaker.isEventOpen("jdk.ObjectAllocationSample"));
    }

    @Test
    void tripStreamBlocksAcquireUntilCleared() {
        JfrMetricsProperties.CircuitBreaker props = new JfrMetricsProperties.CircuitBreaker();
        props.setMaxPerSecond(10_000);
        props.setGlobalMaxPerSecond(10_000);
        props.setCooldown(Duration.ofSeconds(30));
        JfrConsumerCircuitBreaker breaker = new JfrConsumerCircuitBreaker(props);
        assertTrue(breaker.tryAcquire("jdk.ExecutionSample"));
        breaker.tripStream();
        assertTrue(breaker.isStreamOpen());
        assertFalse(breaker.tryAcquire("jdk.ExecutionSample"));
        breaker.clearStream();
        assertFalse(breaker.isStreamOpen());
        assertTrue(breaker.tryAcquire("jdk.ExecutionSample"));
    }

    @Test
    void clearAllEventsRecoversOrphansOutsideTrippedSet() {
        JfrMetricsProperties.CircuitBreaker props = new JfrMetricsProperties.CircuitBreaker();
        props.setMaxPerSecond(1);
        props.setGlobalMaxPerSecond(10_000);
        props.setCooldown(Duration.ofSeconds(1));
        JfrConsumerCircuitBreaker breaker = new JfrConsumerCircuitBreaker(props);
        assertTrue(breaker.tryAcquire("a"));
        assertFalse(breaker.tryAcquire("a"));
        assertTrue(breaker.tryAcquire("b"));
        assertFalse(breaker.tryAcquire("b"));
        assertTrue(breaker.isEventOpen("a"));
        assertTrue(breaker.isEventOpen("b"));
        assertTrue(breaker.openEventCount() >= 2);
        breaker.clearAllEvents();
        assertFalse(breaker.isEventOpen("a"));
        assertFalse(breaker.isEventOpen("b"));
        assertTrue(breaker.tryAcquire("a"));
        assertTrue(breaker.tryAcquire("b"));
    }
}
