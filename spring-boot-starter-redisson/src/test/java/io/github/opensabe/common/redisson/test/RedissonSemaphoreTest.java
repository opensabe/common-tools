package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Import(RedissonSemaphoreTest.Config.class)
public class RedissonSemaphoreTest extends BaseRedissonTest {
    private static final int THREAD_COUNT = 16;

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
        private final RedissonClient redissonClient;
        private final StringRedisTemplate redisTemplate;
        @Getter
        private final AtomicInteger count = new AtomicInteger(0);
        @Getter
        private final AtomicBoolean result = new AtomicBoolean(true);
        @Getter
        private final AtomicBoolean hasReachedMax = new AtomicBoolean(false);

        public TestRedissonRateLimiterClass(RedissonClient redissonClient, StringRedisTemplate redisTemplate) {
            this.redissonClient = redissonClient;
            this.redisTemplate = redisTemplate;
        }

        @RedissonSemaphore(
                name = "testBlockAcquire",
                type = RedissonSemaphore.Type.BLOCK,
                totalPermits = 10
        )
        public void testBlockAcquire() {
            int i = count.incrementAndGet();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (Exception e) {
            }
            if (i > 10) {
                result.set(false);
            }
            if (i == 10) {
                hasReachedMax.set(true);
            }
            count.decrementAndGet();
        }
    }

    @Autowired
    private TestRedissonRateLimiterClass testRedissonRateLimiterClass;

    @Test
    public void testBlockAcquire() throws InterruptedException {
        testRedissonRateLimiterClass.getResult().set(true);
        Thread []threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
               testRedissonRateLimiterClass.testBlockAcquire();
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        Assertions.assertTrue(testRedissonRateLimiterClass.getResult().get());
        Assertions.assertTrue(testRedissonRateLimiterClass.getHasReachedMax().get());
    }

}
