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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.Getter;

@Import(RedissonRateLimiterTest.Config.class)
public class RedissonRateLimiterTest extends BaseRedissonTest {
    private static final int THREAD_COUNT = 10;
    @Autowired
    private TestRedissonRateLimiterClass testRedissonRateLimiterClass;

    @Test
    public void testRateLimiterBlockAcquire() throws InterruptedException {
        List<Long> list = testRedissonRateLimiterClass.getList();
        testRedissonRateLimiterClass.getResult().set(true);
        list.clear();
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                testRedissonRateLimiterClass.testRateLimiterBlockAcquire();
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        for (int i = 1; i < list.size(); i++) {
            //验证限流差不多是 1 秒
            Assertions.assertTrue(list.get(i) - list.get(i - 1) >= 900);
        }
        Assertions.assertEquals(10, list.size());
        Assertions.assertTrue(testRedissonRateLimiterClass.getResult().get());
    }

    @Test
    public void testRateLimiterBlockAcquireWithParams() throws InterruptedException {
        List<Long> list = testRedissonRateLimiterClass.getList();
        testRedissonRateLimiterClass.getResult().set(true);
        list.clear();
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                testRedissonRateLimiterClass.testRateLimiterBlockAcquireWithParams("test");
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        for (int i = 1; i < list.size(); i++) {
            //验证限流差不多是 1 秒
            Assertions.assertTrue(list.get(i) - list.get(i - 1) >= 900);
        }
        Assertions.assertEquals(10, list.size());
        Assertions.assertTrue(testRedissonRateLimiterClass.getResult().get());
    }

    @Test
    public void testRateLimiterBlockAcquireWithParams1() throws InterruptedException {
        List<Long> list = testRedissonRateLimiterClass.getList();
        testRedissonRateLimiterClass.getResult().set(true);
        list.clear();
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                testRedissonRateLimiterClass.testRateLimiterBlockAcquireWithParams1("test1");
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        for (int i = 1; i < list.size(); i++) {
            //验证限流差不多是 1 秒
            Assertions.assertTrue(list.get(i) - list.get(i - 1) >= 900);
        }
        Assertions.assertEquals(10, list.size());
        Assertions.assertTrue(testRedissonRateLimiterClass.getResult().get());
    }

    @Test
    public void testRateLimiterTryAcquireNoWait() throws InterruptedException {
        List<Long> list = testRedissonRateLimiterClass.getList();
        testRedissonRateLimiterClass.getResult().set(true);
        list.clear();
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextLong(9 * 1000));
                    testRedissonRateLimiterClass.testRateLimiterTryAcquireNoWait();
                } catch (Exception e) {
                    //ignore
                }
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        for (int i = 1; i < list.size(); i++) {
            //验证限流差不多是 1 秒
            Assertions.assertTrue(list.get(i) - list.get(i - 1) >= 900);
        }
        Assertions.assertTrue(list.size() < 10);
        Assertions.assertTrue(testRedissonRateLimiterClass.getResult().get());
    }

    @Test
    public void testRateLimiterTryAcquireWithWaitTime() throws InterruptedException {
        List<Long> list = testRedissonRateLimiterClass.getList();
        testRedissonRateLimiterClass.getResult().set(true);
        list.clear();
        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                try {
                    testRedissonRateLimiterClass.testRateLimiterTryAcquireWithWaitTime();
                } catch (Exception e) {
                    //ignore
                }
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        for (int i = 1; i < list.size(); i++) {
            //验证限流差不多是 1 秒
            Assertions.assertTrue(list.get(i) - list.get(i - 1) >= 900);
        }
        Assertions.assertEquals(10, list.size());
        Assertions.assertTrue(testRedissonRateLimiterClass.getResult().get());
    }

    public static class Config {
        @Autowired
        private RedissonClient redissonClient;
        @Autowired
        private StringRedisTemplate redisTemplate;

        @Bean
        public TestRedissonRateLimiterClass testRedissonRateLimiterClass() {
            return new TestRedissonRateLimiterClass(redissonClient, redisTemplate);
        }
    }

    public static class TestRedissonRateLimiterClass {
        @Getter
        private final List<Long> list = new CopyOnWriteArrayList<>();
        private final RedissonClient redissonClient;
        private final StringRedisTemplate redisTemplate;
        @Getter
        private final AtomicBoolean result = new AtomicBoolean(true);

        public TestRedissonRateLimiterClass(RedissonClient redissonClient, StringRedisTemplate redisTemplate) {
            this.redissonClient = redissonClient;
            this.redisTemplate = redisTemplate;
        }

        @RedissonRateLimiter(
                name = "testRateLimiterBlockAcquire",
                type = RedissonRateLimiter.Type.BLOCK,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = TimeUnit.SECONDS
        )
        public void testRateLimiterBlockAcquire() {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter("testRateLimiterBlockAcquire");
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
                System.out.println(JsonUtil.toJSONString(config));
            } catch (Exception e) {
                result.set(false);
            }
        }

        @RedissonRateLimiter(
                name = "testRateLimiterBlockAcquireWithParams",
                type = RedissonRateLimiter.Type.BLOCK,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = TimeUnit.SECONDS
        )
        public void testRateLimiterBlockAcquireWithParams(@RedissonRateLimiterName String permitsName) {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter(RedissonRateLimiter.DEFAULT_PREFIX + permitsName);
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
            } catch (Exception e) {
                result.set(false);
            }
        }

        @RedissonRateLimiter(
                name = "#permitsName",
                type = RedissonRateLimiter.Type.BLOCK,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = TimeUnit.SECONDS
        )
        public void testRateLimiterBlockAcquireWithParams1(String permitsName) {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter(RedissonRateLimiter.DEFAULT_PREFIX + permitsName);
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
            } catch (Exception e) {
                result.set(false);
            }
        }

        @RedissonRateLimiter(
                name = "testRateLimiterTryAcquireNoWait",
                type = RedissonRateLimiter.Type.TRY,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = TimeUnit.SECONDS
        )
        public void testRateLimiterTryAcquireNoWait() {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter("testRateLimiterTryAcquireNoWait");
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
            } catch (Exception e) {
                result.set(false);
            }
        }

        @RedissonRateLimiter(
                name = "testRateLimiterTryAcquireWithWaitTime",
                type = RedissonRateLimiter.Type.TRY,
                waitTime = 15,
                timeUnit = TimeUnit.SECONDS,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = TimeUnit.SECONDS
        )
        public void testRateLimiterTryAcquireWithWaitTime() {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter("testRateLimiterTryAcquireWithWaitTime");
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
            } catch (Exception e) {
                result.set(false);
            }
        }
    }
}
