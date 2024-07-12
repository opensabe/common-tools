package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.test.common.SingleRedisIntegrationTest;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@JfrEventTest
@AutoConfigureObservability
@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.redis.redisson.aop.order=" + RedissonSemaphoreTest.ORDER,
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.lettuce.pool.enabled=true",
        "spring.data.redis.lettuce.pool.max-active=2",
        "spring.data.redis.port=" + SingleRedisIntegrationTest.PORT,
})
public class RedissonSemaphoreTest {
    public static final int ORDER = -100000;
    private static final int THREAD_COUNT = 16;

    public JfrEvents jfrEvents = new JfrEvents();

    @EnableAutoConfiguration
    @Configuration
    public static class App {
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
            } catch (InterruptedException e) {
                e.printStackTrace();
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
