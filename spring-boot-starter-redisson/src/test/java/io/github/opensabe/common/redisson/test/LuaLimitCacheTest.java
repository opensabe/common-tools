package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.test.common.SingleRedisIntegrationTest;
import io.github.opensabe.common.redisson.util.LuaLimitCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicInteger;


@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@AutoConfigureObservability
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.data.redis.host=localhost",
        "spring.data.redis.lettuce.pool.enabled=true",
        "spring.data.redis.lettuce.pool.max-active=2",
        "spring.data.redis.port=" + SingleRedisIntegrationTest.PORT,
},classes = LuaLimitCacheTest.App.class)
public class LuaLimitCacheTest {

    @SpringBootApplication
    public static class App {
    }

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
