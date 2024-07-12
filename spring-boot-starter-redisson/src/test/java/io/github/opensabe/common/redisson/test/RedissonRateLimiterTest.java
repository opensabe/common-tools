package io.github.opensabe.common.redisson.test;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;
import io.github.opensabe.common.redisson.test.common.SingleRedisIntegrationTest;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@JfrEventTest
@AutoConfigureObservability
@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.redis.redisson.aop.order=" + RedissonRateLimiterTest.ORDER,
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.lettuce.pool.enabled=true",
        "spring.data.redis.lettuce.pool.max-active=2",
        "spring.data.redis.port=" + SingleRedisIntegrationTest.PORT,
})
public class RedissonRateLimiterTest {
    public static final int ORDER = -100000;
    private static final int THREAD_COUNT = 10;
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
                rateIntervalUnit = RateIntervalUnit.SECONDS
        )
        public void testRateLimiterBlockAcquire() {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter("testRateLimiterBlockAcquire");
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1, config.getRate());
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
                System.out.println(JSON.toJSONString(config));
            } catch (Exception e) {
                e.printStackTrace();
                result.set(false);
            }
        }

        @RedissonRateLimiter(
                name = "testRateLimiterBlockAcquireWithParams",
                type = RedissonRateLimiter.Type.BLOCK,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = RateIntervalUnit.SECONDS
        )
        public void testRateLimiterBlockAcquireWithParams(@RedissonRateLimiterName String permitsName) {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter(RedissonRateLimiterName.DEFAULT_PREFIX + permitsName);
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1, config.getRate());
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
                System.out.println(JSON.toJSONString(config));
            } catch (Exception e) {
                e.printStackTrace();
                result.set(false);
            }
        }

        @RedissonRateLimiter(
                name = "testRateLimiterTryAcquireNoWait",
                type = RedissonRateLimiter.Type.TRY,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = RateIntervalUnit.SECONDS
        )
        public void testRateLimiterTryAcquireNoWait() {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter("testRateLimiterTryAcquireNoWait");
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1, config.getRate());
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
                System.out.println(JSON.toJSONString(config));
            } catch (Exception e) {
                e.printStackTrace();
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
                rateIntervalUnit = RateIntervalUnit.SECONDS
        )
        public void testRateLimiterTryAcquireWithWaitTime() {
            try {
                list.add(System.currentTimeMillis());
                RRateLimiter testRateLimiterBlockAcquire = redissonClient.getRateLimiter("testRateLimiterTryAcquireWithWaitTime");
                RateLimiterConfig config = testRateLimiterBlockAcquire.getConfig();
                Assertions.assertEquals(1, config.getRate());
                Assertions.assertEquals(1000, config.getRateInterval());
                Assertions.assertEquals(RateType.OVERALL, config.getRateType());
                System.out.println(JSON.toJSONString(config));
            } catch (Exception e) {
                e.printStackTrace();
                result.set(false);
            }
        }
    }

    @Autowired
    private TestRedissonRateLimiterClass testRedissonRateLimiterClass;

    @Test
    public void testRateLimiterBlockAcquire() throws InterruptedException {
        List<Long> list = testRedissonRateLimiterClass.getList();
        testRedissonRateLimiterClass.getResult().set(true);
        list.clear();
        Thread []threads = new Thread[THREAD_COUNT];
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
        Thread []threads = new Thread[THREAD_COUNT];
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
    public void testRateLimiterTryAcquireNoWait() throws InterruptedException {
        List<Long> list = testRedissonRateLimiterClass.getList();
        testRedissonRateLimiterClass.getResult().set(true);
        list.clear();
        Thread []threads = new Thread[THREAD_COUNT];
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
        Thread []threads = new Thread[THREAD_COUNT];
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
}
