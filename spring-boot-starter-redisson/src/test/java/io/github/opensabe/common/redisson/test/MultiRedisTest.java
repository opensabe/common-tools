package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.lettuce.MultiRedisLettuceConnectionFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.data.redis.enable-multi=true",
        "spring.data.redis.multi.default.host=127.0.0.1",
        "spring.data.redis.multi.default.port=6378",
        "spring.data.redis.multi.default.lettuce.pool.enabled=true",
        "spring.data.redis.multi.default.lettuce.pool.max-active=2",
        "spring.data.redis.multi.test.host=127.0.0.1",
        "spring.data.redis.multi.test.port=6380",
        "spring.data.redis.multi.default.lettuce.pool.enabled=true",
        "spring.data.redis.multi.default.lettuce.pool.max-active=2",
})
@Execution(ExecutionMode.SAME_THREAD)
public class MultiRedisTest {


    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }

    @ClassRule
    static GenericContainer redis1 = new FixedHostPortGenericContainer("redis")
            .withFixedExposedPort(6378,6379)
            .withExposedPorts(6379)
            .withCommand("redis-server");
    @ClassRule
    static GenericContainer redis2 = new FixedHostPortGenericContainer("redis")
            .withFixedExposedPort(6380,6379)
            .withExposedPorts(6379)
            .withCommand("redis-server");


    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ReactiveStringRedisTemplate reactiveRedisTemplate;
    @Autowired
    private MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactory;

    @BeforeAll
    static void setup () {
        redis1.start();
        redis2.start();
    }

    @AfterAll
    static void destroy () {
        redis1.stop();
        redis2.stop();
    }

    private void testMulti(String suffix) {
        redisTemplate.opsForValue().set("testDefault" + suffix, "testDefault");
        multiRedisLettuceConnectionFactory.setCurrentRedis("test");
        redisTemplate.opsForValue().set("testSecond" + suffix, "testSecond");
        Assertions.assertTrue(redisTemplate.hasKey("testDefault" + suffix));
        Assertions.assertFalse(redisTemplate.hasKey("testSecond" + suffix));
        multiRedisLettuceConnectionFactory.setCurrentRedis("test");
        Assertions.assertFalse(redisTemplate.hasKey("testDefault" + suffix));
        multiRedisLettuceConnectionFactory.setCurrentRedis("test");
        Assertions.assertTrue(redisTemplate.hasKey("testSecond" + suffix));
    }

    @Test
    public void testMultiBlock() {
        testMulti("");
    }

    @Test
    public void testMultiBlockMultiThread() throws InterruptedException {
        Thread thread[] = new Thread[10];
        AtomicBoolean result = new AtomicBoolean(true);
        for (int i = 0; i < thread.length; i++) {
            int finalI = i;
            thread[i] = new Thread(() -> {
                try {
                    testMulti("" + finalI);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.set(false);
                }
            });
        }
        for (int i = 0; i < thread.length; i++) {
            thread[i].start();
        }
        for (int i = 0; i < thread.length; i++) {
            thread[i].join();
        }
        Assertions.assertTrue(result.get());
    }

    private Mono<Boolean> reactiveMulti(String suffix) {
        return reactiveRedisTemplate.opsForValue().set("testReactiveDefault" + suffix, "testReactiveDefault")
                .flatMap(b -> {
                    multiRedisLettuceConnectionFactory.setCurrentRedis("test");
                    return reactiveRedisTemplate.opsForValue().set("testReactiveSecond" + suffix, "testReactiveSecond");
                }).flatMap(b -> {
                    return reactiveRedisTemplate.hasKey("testReactiveDefault" + suffix);
                }).map(b -> {
                    Assertions.assertTrue(b);
                    System.out.println(Thread.currentThread().getName());
                    return b;
                }).flatMap(b -> {
                    return reactiveRedisTemplate.hasKey("testReactiveSecond" + suffix);
                }).map(b -> {
                    Assertions.assertFalse(b);
                    System.out.println(Thread.currentThread().getName());
                    return b;
                }).flatMap(b -> {
                    multiRedisLettuceConnectionFactory.setCurrentRedis("test");
                    return reactiveRedisTemplate.hasKey("testReactiveDefault" + suffix);
                }).map(b -> {
                    Assertions.assertFalse(b);
                    System.out.println(Thread.currentThread().getName());
                    return b;
                }).flatMap(b -> {
                    multiRedisLettuceConnectionFactory.setCurrentRedis("test");
                    return reactiveRedisTemplate.hasKey("testReactiveSecond" + suffix);
                }).map(b -> {
                    Assertions.assertTrue(b);
                    return b;
                });
    }

    @Test
    public void testMultiReactive() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            reactiveMulti("" + i).subscribe(System.out::println);
        }
        TimeUnit.SECONDS.sleep(10);
    }
}
