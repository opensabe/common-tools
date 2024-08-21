package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.github.opensabe.common.redisson.util.LuaLimitCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

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
