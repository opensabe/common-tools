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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Rate-limits consumer callbacks. Tripping only unsubscribes handlers — never calls JFR disable/enable.
 */
final class JfrConsumerCircuitBreaker {

    private final int maxPerSecond;
    private final int globalMaxPerSecond;
    private final long cooldownNanos;

    private final ConcurrentHashMap<String, Window> perEvent = new ConcurrentHashMap<>();
    private final Window global = new Window();
    private final ConcurrentHashMap<String, AtomicBoolean> open = new ConcurrentHashMap<>();
    private final AtomicBoolean streamOpen = new AtomicBoolean(false);
    private final AtomicLong streamOpenUntil = new AtomicLong(0L);
    private final LongAdder dropped = new LongAdder();

    JfrConsumerCircuitBreaker(JfrMetricsProperties.CircuitBreaker properties) {
        this.maxPerSecond = Math.max(1, properties.getMaxPerSecond());
        this.globalMaxPerSecond = Math.max(1, properties.getGlobalMaxPerSecond());
        this.cooldownNanos = Math.max(1L, properties.getCooldown().toNanos());
    }

    /**
     * @return true if the event may be aggregated; false if dropped or circuit open
     */
    boolean tryAcquire(String eventName) {
        long now = System.nanoTime();
        if (streamOpen.get()) {
            if (now < streamOpenUntil.get()) {
                dropped.increment();
                return false;
            }
            streamOpen.set(false);
        }
        AtomicBoolean eventOpen = open.computeIfAbsent(eventName, ignored -> new AtomicBoolean(false));
        if (eventOpen.get()) {
            dropped.increment();
            return false;
        }
        Window eventWindow = perEvent.computeIfAbsent(eventName, ignored -> new Window());
        long eventRate = eventWindow.hit(now);
        long globalRate = global.hit(now);
        if (eventRate > maxPerSecond) {
            eventOpen.set(true);
            dropped.increment();
            return false;
        }
        if (globalRate > globalMaxPerSecond) {
            tripStream(now);
            dropped.increment();
            return false;
        }
        return true;
    }

    void noteEventTripped(String eventName) {
        open.computeIfAbsent(eventName, ignored -> new AtomicBoolean(false)).set(true);
    }

    boolean isEventOpen(String eventName) {
        AtomicBoolean flag = open.get(eventName);
        return flag != null && flag.get();
    }

    boolean isStreamOpen() {
        return streamOpen.get() && System.nanoTime() < streamOpenUntil.get();
    }

    void clearEvent(String eventName) {
        AtomicBoolean flag = open.get(eventName);
        if (flag != null) {
            flag.set(false);
        }
        Window window = perEvent.get(eventName);
        if (window != null) {
            window.reset();
        }
    }

    void clearStream() {
        streamOpen.set(false);
        streamOpenUntil.set(0L);
        global.reset();
    }

    /** Clears every per-event open flag (used after stream cooldown recovery). */
    void clearAllEvents() {
        for (String eventName : open.keySet()) {
            clearEvent(eventName);
        }
    }

    /** Opens the stream circuit for {@link #cooldownNanos()} (handler unsubscribe only). */
    void tripStream() {
        tripStream(System.nanoTime());
    }

    int openEventCount() {
        int count = 0;
        for (AtomicBoolean flag : open.values()) {
            if (flag.get()) {
                count++;
            }
        }
        return count;
    }

    Map<String, Boolean> openSnapshot() {
        Map<String, Boolean> snapshot = new ConcurrentHashMap<>();
        open.forEach((k, v) -> snapshot.put(k, v.get()));
        return snapshot;
    }

    long drainDropped() {
        return dropped.sumThenReset();
    }

    long cooldownNanos() {
        return cooldownNanos;
    }

    private void tripStream(long now) {
        streamOpen.set(true);
        streamOpenUntil.set(now + cooldownNanos);
    }

    private static final class Window {
        private final AtomicLong secondStart = new AtomicLong(0L);
        private final LongAdder count = new LongAdder();

        long hit(long nowNanos) {
            long start = secondStart.get();
            if (start == 0L || nowNanos - start >= 1_000_000_000L) {
                if (secondStart.compareAndSet(start, nowNanos)) {
                    count.reset();
                }
            }
            count.increment();
            return count.sum();
        }

        void reset() {
            count.reset();
            secondStart.set(0L);
        }
    }
}
