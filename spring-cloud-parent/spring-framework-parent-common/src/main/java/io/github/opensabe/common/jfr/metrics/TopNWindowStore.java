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
import java.util.concurrent.locks.ReentrantLock;

/**
 * Per-window aggregations with a hard cap on distinct keys.
 * {@link #drainTopN(int)} is atomic w.r.t. {@link #add}/{@link #observeMax} so updates are not lost between windows.
 */
final class TopNWindowStore {

    private final ConcurrentHashMap<String, LongAdder> sums = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> maxes = new ConcurrentHashMap<>();
    private final AtomicLong droppedKeys = new AtomicLong();
    private final int maxKeys;
    private final ReentrantLock drainLock = new ReentrantLock();

    TopNWindowStore(int maxKeys) {
        this.maxKeys = Math.max(1, maxKeys);
    }

    boolean add(String key, long delta) {
        if (key == null || key.isEmpty() || delta == 0L) {
            return false;
        }
        drainLock.lock();
        try {
            LongAdder existing = sums.get(key);
            if (existing == null) {
                if (sums.size() >= maxKeys) {
                    droppedKeys.incrementAndGet();
                    return false;
                }
                existing = sums.computeIfAbsent(key, ignored -> new LongAdder());
                if (sums.size() > maxKeys && !sums.containsKey(key)) {
                    droppedKeys.incrementAndGet();
                    return false;
                }
            }
            existing.add(delta);
            return true;
        } finally {
            drainLock.unlock();
        }
    }

    boolean observeMax(String key, long value) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        drainLock.lock();
        try {
            AtomicLong existing = maxes.get(key);
            if (existing == null) {
                if (maxes.size() >= maxKeys) {
                    droppedKeys.incrementAndGet();
                    return false;
                }
                existing = maxes.computeIfAbsent(key, ignored -> new AtomicLong(Long.MIN_VALUE));
            }
            existing.accumulateAndGet(value, Math::max);
            return true;
        } finally {
            drainLock.unlock();
        }
    }

    /**
     * Snapshot top-N by sum and by max independently, then clear the window.
     */
    Snapshot drainTopN(int topN) {
        drainLock.lock();
        try {
            List<Map.Entry<String, Long>> rankedSums = new ArrayList<>(sums.size());
            for (Map.Entry<String, LongAdder> entry : sums.entrySet()) {
                rankedSums.add(Map.entry(entry.getKey(), entry.getValue().sum()));
            }
            rankedSums.sort(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed());

            Map<String, Long> topSums = new LinkedHashMap<>();
            int sumLimit = Math.min(topN, rankedSums.size());
            for (int i = 0; i < sumLimit; i++) {
                Map.Entry<String, Long> entry = rankedSums.get(i);
                topSums.put(entry.getKey(), entry.getValue());
            }

            List<Map.Entry<String, Long>> rankedMax = new ArrayList<>();
            for (Map.Entry<String, AtomicLong> entry : maxes.entrySet()) {
                long value = entry.getValue().get();
                if (value != Long.MIN_VALUE) {
                    rankedMax.add(Map.entry(entry.getKey(), value));
                }
            }
            rankedMax.sort(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed());
            Map<String, Long> topMaxes = new LinkedHashMap<>();
            int maxLimit = Math.min(topN, rankedMax.size());
            for (int i = 0; i < maxLimit; i++) {
                Map.Entry<String, Long> entry = rankedMax.get(i);
                topMaxes.put(entry.getKey(), entry.getValue());
            }

            Map<String, Long> allSums = new LinkedHashMap<>(rankedSums.size());
            for (Map.Entry<String, Long> entry : rankedSums) {
                allSums.put(entry.getKey(), entry.getValue());
            }

            sums.clear();
            maxes.clear();
            long dropped = droppedKeys.getAndSet(0L);
            return new Snapshot(topSums, topMaxes, allSums, dropped);
        } finally {
            drainLock.unlock();
        }
    }

    record Snapshot(Map<String, Long> sums, Map<String, Long> maxes, Map<String, Long> allSums, long droppedKeys) {
        Snapshot(Map<String, Long> sums, Map<String, Long> maxes, long droppedKeys) {
            this(sums, maxes, sums, droppedKeys);
        }
    }
}
