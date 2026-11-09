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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Per-window aggregations with a hard cap on distinct keys to bound allocation under pressure.
 */
final class TopNWindowStore {

    private final ConcurrentHashMap<String, LongAdder> sums = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> maxes = new ConcurrentHashMap<>();
    private final AtomicLong droppedKeys = new AtomicLong();
    private final int maxKeys;

    TopNWindowStore(int maxKeys) {
        this.maxKeys = Math.max(1, maxKeys);
    }

    boolean add(String key, long delta) {
        if (key == null || key.isEmpty() || delta == 0L) {
            return false;
        }
        LongAdder existing = sums.get(key);
        if (existing == null) {
            if (sums.size() >= maxKeys) {
                droppedKeys.incrementAndGet();
                return false;
            }
            existing = sums.computeIfAbsent(key, ignored -> {
                if (sums.size() >= maxKeys) {
                    return null;
                }
                return new LongAdder();
            });
            if (existing == null) {
                droppedKeys.incrementAndGet();
                return false;
            }
        }
        existing.add(delta);
        return true;
    }

    boolean observeMax(String key, long value) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        AtomicLong existing = maxes.get(key);
        if (existing == null) {
            if (maxes.size() >= maxKeys && !maxes.containsKey(key)) {
                if (sums.size() >= maxKeys && !sums.containsKey(key)) {
                    droppedKeys.incrementAndGet();
                    return false;
                }
            }
            existing = maxes.computeIfAbsent(key, ignored -> {
                if (maxes.size() >= maxKeys) {
                    return null;
                }
                return new AtomicLong(Long.MIN_VALUE);
            });
            if (existing == null) {
                droppedKeys.incrementAndGet();
                return false;
            }
        }
        existing.accumulateAndGet(value, Math::max);
        return true;
    }

    /**
     * Snapshot top-N by sum descending, then clear window.
     */
    Snapshot drainTopN(int topN) {
        List<Map.Entry<String, Long>> ranked = new ArrayList<>(sums.size());
        for (Map.Entry<String, LongAdder> entry : sums.entrySet()) {
            ranked.add(Map.entry(entry.getKey(), entry.getValue().sum()));
        }
        ranked.sort(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed());
        int limit = Math.min(topN, ranked.size());
        Map<String, Long> topSums = new LinkedHashMap<>(limit);
        Map<String, Long> topMaxes = new LinkedHashMap<>(limit);
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Long> entry = ranked.get(i);
            topSums.put(entry.getKey(), entry.getValue());
            AtomicLong max = maxes.get(entry.getKey());
            if (max != null && max.get() != Long.MIN_VALUE) {
                topMaxes.put(entry.getKey(), max.get());
            }
        }
        // Also include max-only keys that did not appear in sums ranking if space remains
        if (topMaxes.size() < topN) {
            List<Map.Entry<String, Long>> maxRanked = new ArrayList<>();
            for (Map.Entry<String, AtomicLong> entry : maxes.entrySet()) {
                if (!topMaxes.containsKey(entry.getKey()) && entry.getValue().get() != Long.MIN_VALUE) {
                    maxRanked.add(Map.entry(entry.getKey(), entry.getValue().get()));
                }
            }
            maxRanked.sort(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed());
            for (Map.Entry<String, Long> entry : maxRanked) {
                if (topMaxes.size() >= topN) {
                    break;
                }
                topMaxes.put(entry.getKey(), entry.getValue());
            }
        }
        sums.clear();
        maxes.clear();
        long dropped = droppedKeys.getAndSet(0L);
        return new Snapshot(topSums, topMaxes, dropped);
    }

    int keyCount() {
        return Math.max(sums.size(), maxes.size());
    }

    record Snapshot(Map<String, Long> sums, Map<String, Long> maxes, long droppedKeys) {
    }
}
