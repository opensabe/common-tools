package io.github.opensabe.common.redisson.test.jfr;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.test.common.SingleRedisIntegrationTest;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        SpringExtension.class, SingleRedisIntegrationTest.class
})
@AutoConfigureObservability
@JfrEventTest
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.lettuce.pool.enabled=true",
        "spring.data.redis.lettuce.pool.max-active=2",
        "spring.data.redis.port=" + SingleRedisIntegrationTest.PORT,
})
public class TestRedissonSemaphoreJFR {
    public JfrEvents jfrEvents = new JfrEvents();

    private static final int THREAD_COUNT = 10;

    @EnableAutoConfiguration
    @Configuration
    public static class App {
        @Bean
        public TestRedissonSemaphoreBean testRedissonSemaphoreBean() {
            return new TestRedissonSemaphoreBean();
        }
    }

    public static class TestRedissonSemaphoreBean {
        @RedissonSemaphore(
                name = "TestRedissonSemaphoreJFR-testTryAcquire",
                type = RedissonSemaphore.Type.TRY,
                totalPermits = 1,
                waitTime = 50,
                timeUnit = TimeUnit.MILLISECONDS

        )
        public void testTryAcquire() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    @Autowired
    private TestRedissonSemaphoreBean testRedissonSemaphoreBean;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Test
    public void testTryAcquire() throws InterruptedException {
        Thread []threads = new Thread[THREAD_COUNT];
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                observation.scoped(() -> {
                    try {
                        testRedissonSemaphoreBean.testTryAcquire();
                    } catch (InterruptedException e) {
                        //ignore
                    }
                });
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            TimeUnit.MILLISECONDS.sleep(20);
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        jfrEvents.awaitEvents();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        List<RecordedEvent> rPermitSemaphoreModifiedJFREvents = jfrEvents.events()
                .filter(recordedEvent -> recordedEvent.getEventType().getName().equals("io.github.opensabe.common.redisson.jfr.RPermitSemaphoreModifiedJFREvent"))
                .filter(recordedEvent -> recordedEvent.getString("permitSemaphoreName").equals("TestRedissonSemaphoreJFR-testTryAcquire"))
                .collect(Collectors.toList());
        List<RecordedEvent> rPermitSemaphoreAcquiredJFREvents = jfrEvents.events()
                .filter(recordedEvent -> recordedEvent.getEventType().getName().equals("io.github.opensabe.common.redisson.jfr.RPermitSemaphoreAcquiredJFREvent"))
                .filter(recordedEvent -> recordedEvent.getString("permitSemaphoreName").equals("TestRedissonSemaphoreJFR-testTryAcquire"))
                .collect(Collectors.toList());
        List<RecordedEvent> rPermitSemaphoreReleasedJFREvents = jfrEvents.events()
                .filter(recordedEvent -> recordedEvent.getEventType().getName().equals("io.github.opensabe.common.redisson.jfr.RPermitSemaphoreReleasedJFREvent"))
                .filter(recordedEvent -> recordedEvent.getString("permitSemaphoreName").equals("TestRedissonSemaphoreJFR-testTryAcquire"))
                .collect(Collectors.toList());
        assertEquals(THREAD_COUNT, rPermitSemaphoreModifiedJFREvents.size());
        for (RecordedEvent recordedEvent : rPermitSemaphoreModifiedJFREvents) {
            assertEquals(recordedEvent.getString("modified"), "trySetPermits: 1");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(THREAD_COUNT, rPermitSemaphoreModifiedJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());
        //只有一个设置 permits 成功
        assertEquals(1, rPermitSemaphoreModifiedJFREvents.stream().filter(recordedEvent -> recordedEvent.getBoolean("modifiedSuccessfully")).count());


        assertEquals(THREAD_COUNT, rPermitSemaphoreAcquiredJFREvents.size());
        for (RecordedEvent recordedEvent : rPermitSemaphoreAcquiredJFREvents) {
            assertTrue(recordedEvent.getBoolean("tryAcquire"));
            assertEquals(recordedEvent.getLong("waitTime"), 50);
            assertEquals(recordedEvent.getLong("leaseTime"), -1);
            assertEquals(recordedEvent.getString("unit"), "MILLISECONDS");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(THREAD_COUNT, rPermitSemaphoreAcquiredJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());

        List<String> acquired = rPermitSemaphoreAcquiredJFREvents.stream().map(recordedEvent ->recordedEvent.getString("permitId")).filter(Objects::nonNull).collect(Collectors.toList());

        assertEquals(acquired.size(), rPermitSemaphoreReleasedJFREvents.size());
        for (RecordedEvent recordedEvent : rPermitSemaphoreReleasedJFREvents) {
            assertTrue(recordedEvent.getBoolean("permitReleasedSuccessfully"));
            assertTrue(acquired.contains(recordedEvent.getString("permitId")));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(acquired.size(), rPermitSemaphoreReleasedJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());

    }
}
