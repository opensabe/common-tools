package io.github.opensabe.common.idgenerator.test.service;

import cn.hutool.core.collection.ConcurrentHashSet;
import io.github.opensabe.common.idgenerator.service.UniqueIDWithouBizType;
import io.github.opensabe.common.idgenerator.test.common.BaseUniqueIdTest;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.Set;

public class UniqueIDWithoutBizTypeImplTest extends BaseUniqueIdTest {

    @Autowired
    private UniqueIDWithouBizType uniqueID;

    private static final int THREAD_COUNT = 15;
    private static final int GET_COUNT = 100;

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

