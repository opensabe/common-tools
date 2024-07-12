package io.github.opensabe.common.idgenerator.service;

import cn.hutool.core.collection.ConcurrentHashSet;
import io.github.opensabe.common.idgenerator.service.UniqueIDWithouBizType;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "defaultOperId=2"
})
public class UniqueIDWithoutBizTypeImplTest {

    @ClassRule
    @SuppressWarnings({"deprecation", "resource"})
    public static GenericContainer<?> redis = new FixedHostPortGenericContainer<>("redis")
                .withFixedExposedPort(6379, 6379)
                .withExposedPorts(6379)
                .withCommand("redis-server");


    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }

    @Autowired
    private UniqueIDWithouBizType uniqueID;

    private static final int THREAD_COUNT = 15;
    private static final int GET_COUNT = 100;


    @BeforeAll
    static void setup () {
        redis.start();
    }

    @AfterAll
    static void destroy () {
        redis.stop();
    }

    @Test
    public void testMultiThreadGetUniqueId() throws InterruptedException {
        Set<Long> ids = new ConcurrentHashSet<>();
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
        System.out.println(ids);
    }

    @Test
    public void testMultiThreadGetShortUniqueId() throws InterruptedException {
        Set<Long> ids = new ConcurrentHashSet<>();
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
        System.out.println(ids);
    }
}

