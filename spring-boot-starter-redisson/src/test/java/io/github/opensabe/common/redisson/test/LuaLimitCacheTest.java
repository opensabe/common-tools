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
package io.github.opensabe.common.redisson.test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.github.opensabe.common.redisson.util.LuaLimitCache;

public class LuaLimitCacheTest extends BaseRedissonTest {

    @Autowired
    private LuaLimitCache luaLimitCache;

    @Test
    public void testMultiThread() throws InterruptedException {
        Thread[] threads = new Thread[10];
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 2; j++) {
                    boolean test = luaLimitCache.isReachLimit(
                            "test",
                            100L, 1L, 50L,
                            () -> 50L);
                    if (test) {
                        atomicInteger.incrementAndGet();
                    }
                }
            });
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        System.out.println(atomicInteger.get());
    }
}
