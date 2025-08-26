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
package io.github.opensabe.common.idgenerator.test.service;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cn.hutool.core.collection.ConcurrentHashSet;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.idgenerator.test.common.BaseUniqueIdWithValkeyTest;

public class UniqueIDImplWithValkeyTest extends BaseUniqueIdWithValkeyTest {

    private static final int THREAD_COUNT = 15;
    private static final int GET_COUNT = 100;
    @Autowired
    private UniqueID uniqueID;

    @Test
    public void testMultiThreadGetUniqueId() throws InterruptedException {
        Set<String> ids = new ConcurrentHashSet<>();
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < GET_COUNT; j++) {
                    ids.add(uniqueID.getUniqueId("biz"));
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        Assertions.assertEquals(ids.size(), THREAD_COUNT * GET_COUNT);
    }

    @Test
    public void testMultiThreadGetShortUniqueId() throws InterruptedException {
        Set<String> ids = new ConcurrentHashSet<>();
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < GET_COUNT; j++) {
                    ids.add(uniqueID.getShortUniqueId("biz"));
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        Assertions.assertEquals(ids.size(), THREAD_COUNT * GET_COUNT);
    }
}

