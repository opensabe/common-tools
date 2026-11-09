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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

/**
 * Publishes previous flush-window Top-N values as Gauges. Dropping gauges is safe (not Counters).
 */
final class JfrMetricsPublisher {

    static final String ALLOC_SAMPLE_BY_CLASS = "jfr_alloc_sample_weight_bytes_by_class";
    static final String ALLOC_SAMPLE_BY_METHOD = "jfr_alloc_sample_weight_bytes_by_method";
    static final String OUTSIDE_TLAB_BY_CLASS = "jfr_outside_tlab_bytes_by_class";
    static final String ALLOC_REQ_GC_BY_METHOD = "jfr_alloc_requiring_gc_bytes_by_method";
    static final String ALLOC_REQ_GC_MAX_BY_METHOD = "jfr_alloc_requiring_gc_max_bytes_by_method";
    static final String OLD_OBJECT_SAMPLES = "jfr_old_object_samples_by_class";
    static final String OLD_OBJECT_HEAP = "jfr_old_object_heap_bytes";
    static final String EXECUTION_SAMPLES = "jfr_execution_samples_by_method";
    /** Window wait time in milliseconds (Grafana: / 1000 for seconds). */
    static final String MONITOR_WAIT = "jfr_monitor_enter_wait_milliseconds";
    static final String MONITOR_COUNT = "jfr_monitor_enter_count";
    static final String Z_STALL = "jfr_z_allocation_stall_seconds";

    private final MeterRegistry registry;
    private final int topN;
    private final Map<MeterKey, AtomicLong> values = new ConcurrentHashMap<>();
    private final AtomicLong repositoryAttached = new AtomicLong(0);
    private final AtomicLong aggregatorKeys = new AtomicLong(0);
    private final AtomicLong droppedTotal = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> eventSeen = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> circuitOpen = new ConcurrentHashMap<>();
    private final AtomicLong oldObjectHeap = new AtomicLong(0);
    private final AtomicReference<Double> zStallSeconds = new AtomicReference<>(0d);

    JfrMetricsPublisher(MeterRegistry registry, int topN) {
        this.registry = registry;
        this.topN = topN;
        Gauge.builder("jfr_metrics_repository_attached", repositoryAttached, AtomicLong::get)
                .description("1 when EventStream is attached to a disk JFR repository")
                .register(registry);
        Gauge.builder("jfr_metrics_aggregator_keys", aggregatorKeys, AtomicLong::get)
                .description("Approximate distinct aggregation keys in the last window")
                .register(registry);
        Gauge.builder("jfr_metrics_dropped_total", droppedTotal, AtomicLong::get)
                .description("Events/keys dropped by consumer circuit breaker or map cap (last flush)")
                .register(registry);
        Gauge.builder(OLD_OBJECT_HEAP, oldObjectHeap, AtomicLong::get)
                .description("Latest observed OldObjectSample lastKnownHeapUsage (global heap, not per-class)")
                .register(registry);
        Gauge.builder(Z_STALL, zStallSeconds, AtomicReference::get)
                .description("ZAllocationStall duration seconds in last window")
                .register(registry);
    }

    void setRepositoryAttached(boolean attached) {
        repositoryAttached.set(attached ? 1L : 0L);
    }

    void markEventSeen(String eventName) {
        eventSeen.computeIfAbsent(eventName, name -> {
            AtomicLong value = new AtomicLong(0);
            Gauge.builder("jfr_metrics_event_seen", value, AtomicLong::get)
                    .tag("event", name)
                    .description("1 if this event name was observed in the last window")
                    .register(registry);
            return value;
        }).set(1L);
    }

    void setCircuitOpen(String eventName, boolean open) {
        circuitOpen.computeIfAbsent(eventName, name -> {
            AtomicLong value = new AtomicLong(0);
            Gauge.builder("jfr_metrics_circuit_open", value, AtomicLong::get)
                    .tag("event", name)
                    .description("1 when consumer unsubscribed this event due to rate limit")
                    .register(registry);
            return value;
        }).set(open ? 1L : 0L);
    }

    void setStreamCircuitOpen(boolean open) {
        setCircuitOpen("_stream_", open);
    }

    void publishLabeled(String metric, String tagName, TopNWindowStore.Snapshot snapshot) {
        Set<MeterKey> keep = new HashSet<>();
        int i = 0;
        for (Map.Entry<String, Long> entry : snapshot.sums().entrySet()) {
            if (i++ >= topN) {
                break;
            }
            MeterKey key = new MeterKey(metric, tagName, entry.getKey());
            keep.add(key);
            AtomicLong holder = values.computeIfAbsent(key, this::registerLabeled);
            holder.set(entry.getValue());
        }
        removeMissing(metric, tagName, keep);
    }

    void publishLabeledMax(String metric, String tagName, TopNWindowStore.Snapshot snapshot) {
        Set<MeterKey> keep = new HashSet<>();
        int i = 0;
        for (Map.Entry<String, Long> entry : snapshot.maxes().entrySet()) {
            if (i++ >= topN) {
                break;
            }
            MeterKey key = new MeterKey(metric, tagName, entry.getKey());
            keep.add(key);
            AtomicLong holder = values.computeIfAbsent(key, this::registerLabeled);
            holder.set(entry.getValue());
        }
        removeMissing(metric, tagName, keep);
    }

    void publishMonitor(TopNWindowStore.Snapshot waitNanos, TopNWindowStore.Snapshot counts) {
        Set<MeterKey> keepWait = new HashSet<>();
        Set<MeterKey> keepCount = new HashSet<>();
        int i = 0;
        for (Map.Entry<String, Long> entry : waitNanos.sums().entrySet()) {
            if (i++ >= topN) {
                break;
            }
            String composite = entry.getKey();
            int split = composite.indexOf('|');
            String method = split < 0 ? composite : composite.substring(0, split);
            String monitor = split < 0 ? "unknown" : composite.substring(split + 1);
            MeterKey waitKey = new MeterKey(MONITOR_WAIT, method, monitor);
            MeterKey countKey = new MeterKey(MONITOR_COUNT, method, monitor);
            keepWait.add(waitKey);
            keepCount.add(countKey);
            values.computeIfAbsent(waitKey, k -> registerMonitor(MONITOR_WAIT, k)).set(entry.getValue());
            long count = counts.sums().getOrDefault(composite, 0L);
            values.computeIfAbsent(countKey, k -> registerMonitor(MONITOR_COUNT, k)).set(count);
        }
        removeMissingMonitor(MONITOR_WAIT, keepWait);
        removeMissingMonitor(MONITOR_COUNT, keepCount);
    }

    void setOldObjectHeap(long bytes) {
        oldObjectHeap.set(bytes);
    }

    void setZStallSeconds(double seconds) {
        zStallSeconds.set(seconds);
    }

    void setAggregatorKeys(long keys) {
        aggregatorKeys.set(keys);
    }

    void addDropped(long dropped) {
        droppedTotal.addAndGet(dropped);
    }

    void resetEventSeen() {
        eventSeen.values().forEach(v -> v.set(0L));
    }

    private AtomicLong registerLabeled(MeterKey key) {
        AtomicLong value = new AtomicLong(0);
        Gauge.builder(key.metric(), value, AtomicLong::get)
                .tag(key.tagName(), key.tagValue())
                .register(registry);
        return value;
    }

    private AtomicLong registerMonitor(String metric, MeterKey key) {
        AtomicLong value = new AtomicLong(0);
        Gauge.builder(metric, value, AtomicLong::get)
                .tag("method", key.tagName())
                .tag("monitor_class", key.tagValue())
                .register(registry);
        return value;
    }

    private void removeMissing(String metric, String tagName, Set<MeterKey> keep) {
        Set<MeterKey> toRemove = new HashSet<>();
        for (MeterKey key : values.keySet()) {
            if (key.metric().equals(metric) && key.tagName().equals(tagName) && !keep.contains(key)) {
                toRemove.add(key);
            }
        }
        for (MeterKey key : toRemove) {
            values.remove(key);
            registry.remove(new Meter.Id(metric, Tags.of(tagName, key.tagValue()), null, null, Meter.Type.GAUGE));
        }
    }

    private void removeMissingMonitor(String metric, Set<MeterKey> keep) {
        Set<MeterKey> toRemove = new HashSet<>();
        for (MeterKey key : values.keySet()) {
            if (key.metric().equals(metric) && !keep.contains(key)) {
                toRemove.add(key);
            }
        }
        for (MeterKey key : toRemove) {
            values.remove(key);
            registry.remove(new Meter.Id(
                    metric,
                    Tags.of("method", key.tagName(), "monitor_class", key.tagValue()),
                    null,
                    null,
                    Meter.Type.GAUGE));
        }
    }

    private record MeterKey(String metric, String tagName, String tagValue) {
    }
}
