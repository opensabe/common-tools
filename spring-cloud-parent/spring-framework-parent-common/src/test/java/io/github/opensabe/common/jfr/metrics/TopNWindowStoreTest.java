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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopNWindowStoreTest {

    @Test
    void drainsTopNBySumAndClearsWindow() {
        TopNWindowStore store = new TopNWindowStore(10);
        store.add("a", 10);
        store.add("b", 30);
        store.add("c", 20);
        TopNWindowStore.Snapshot snapshot = store.drainTopN(2);
        assertEquals(2, snapshot.sums().size());
        assertEquals(30L, snapshot.sums().get("b"));
        assertEquals(20L, snapshot.sums().get("c"));
        assertTrue(store.drainTopN(5).sums().isEmpty());
    }

    @Test
    void dropsNewKeysWhenCapReached() {
        TopNWindowStore store = new TopNWindowStore(2);
        assertTrue(store.add("a", 1));
        assertTrue(store.add("b", 1));
        assertTrue(!store.add("c", 1));
        TopNWindowStore.Snapshot snapshot = store.drainTopN(10);
        assertEquals(2, snapshot.sums().size());
        assertTrue(snapshot.droppedKeys() >= 1);
    }

    @Test
    void tracksMaxPerKey() {
        TopNWindowStore store = new TopNWindowStore(10);
        store.add("m", 5);
        store.observeMax("m", 100);
        store.observeMax("m", 40);
        TopNWindowStore.Snapshot snapshot = store.drainTopN(5);
        assertEquals(100L, snapshot.maxes().get("m"));
    }

    @Test
    void ranksMaxIndependentlyFromSums() {
        TopNWindowStore store = new TopNWindowStore(10);
        store.add("small-sum-big-max", 1);
        store.observeMax("small-sum-big-max", 1000);
        store.add("big-sum-small-max", 500);
        store.observeMax("big-sum-small-max", 2);
        TopNWindowStore.Snapshot snapshot = store.drainTopN(1);
        assertEquals(500L, snapshot.sums().get("big-sum-small-max"));
        assertEquals(1000L, snapshot.maxes().get("small-sum-big-max"));
    }
}
