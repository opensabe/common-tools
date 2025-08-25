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

import io.github.opensabe.common.redisson.lettuce.MultiRedisLettuceConnectionFactory;
import org.junit.jupiter.api.Assertions;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.atomic.AtomicBoolean;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.data.redis.enable-multi=true",
        "spring.data.redis.multi.default.host=127.0.0.1",
        "spring.data.redis.multi.default.port=6378",
        "spring.data.redis.multi.default.lettuce.pool.enabled=true",
        "spring.data.redis.multi.default.lettuce.pool.max-active=100",
        "spring.data.redis.multi.test.host=127.0.0.1",
        "spring.data.redis.multi.test.port=6380",
        "spring.data.redis.multi.default.lettuce.pool.enabled=true",
        "spring.data.redis.multi.default.lettuce.pool.max-active=100",
})
@Execution(ExecutionMode.SAME_THREAD)
public class MultiRedisTest {


    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }

    @Container
    static GenericContainer redis1 = new FixedHostPortGenericContainer("redis")
            .withFixedExposedPort(6378,6379)
            .withExposedPorts(6379)
            .withCommand("redis-server");
    @Container
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
}
