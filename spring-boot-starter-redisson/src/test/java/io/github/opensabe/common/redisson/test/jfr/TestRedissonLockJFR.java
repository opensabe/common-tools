package io.github.opensabe.common.redisson.test.jfr;

import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
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
public class TestRedissonLockJFR {
    static class TestBean {
        @RedissonLock(
                lockType = RedissonLock.BLOCK_LOCK
        )
        public void testBlockLock(@RedissonLockName(prefix = "TestRedissonLockJFR:testBlockLock:") String id) throws InterruptedException {
            //threshold 100ms
            TimeUnit.MILLISECONDS.sleep(100);
        }
        @RedissonLock(
                lockType = RedissonLock.BLOCK_LOCK
        )
        public void testBlockLockException(@RedissonLockName(prefix = "TestRedissonLockJFR:testBlockLockException:") String id) throws InterruptedException {
            //threshold 100ms
            TimeUnit.MILLISECONDS.sleep(100);
            throw new RuntimeException("test");
        }
        @RedissonLock(
                lockType = RedissonLock.TRY_LOCK,
                waitTime = 2000l,
                timeUnit = TimeUnit.MILLISECONDS
        )
        public void testTryLock(@RedissonLockName(prefix = "TestRedissonLockJFR:testTryLock:") String id) throws InterruptedException {
            //threshold 100ms
            TimeUnit.MILLISECONDS.sleep(100);
        }
        @RedissonLock(
                lockType = RedissonLock.TRY_LOCK,
                waitTime = 10l,
                timeUnit = TimeUnit.MILLISECONDS
        )
        public void testTryLockWaitTimeOut(@RedissonLockName(prefix = "TestRedissonLockJFR:testTryLockWaitTimeOut:") String id) throws InterruptedException {
            //threshold 100ms
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }
    @SpringBootApplication
    public static class App {
        @Bean
        public TestBean testBean() {
            return new TestBean();
        }
    }

    @Autowired
    private TestBean testBean;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    public JfrEvents jfrEvents = new JfrEvents();

    private static final int COUNT_OF_THREADS = 12;

    @Test
    public void testBlockLockNormal() {
        Thread [] threads = new Thread[COUNT_OF_THREADS];
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        for (int i = 0; i < threads.length; i++) {
            int finalI = i % 3;
            threads[i] = new Thread(() -> {
                observation.scoped(() -> {
                    try {
                        testBean.testBlockLock(String.valueOf(finalI));
                    } catch (InterruptedException e) {
                        //ignore
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                //ignore
            }
        }
        jfrEvents.awaitEvents();
        List<RecordedEvent> rLockAcquiredJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockAcquiredJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testBlockLock:")
        ).toList();
        List<RecordedEvent> rLockReleasedJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockReleasedJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testBlockLock:")
        ).toList();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.size());
        for (RecordedEvent recordedEvent : rLockAcquiredJFREvents) {
            assertFalse(recordedEvent.getBoolean("tryAcquire"));
            assertEquals(-1, recordedEvent.getLong("waitTime"));
            assertEquals(-1, recordedEvent.getLong("leaseTime"));
            assertEquals(recordedEvent.getString("timeUnit"), "MILLISECONDS");
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertTrue(recordedEvent.getBoolean("lockAcquiredSuccessfully"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());

        assertEquals(COUNT_OF_THREADS, rLockReleasedJFREvents.size());
        for (RecordedEvent recordedEvent : rLockReleasedJFREvents) {
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertTrue(recordedEvent.getBoolean("lockReleasedSuccessfully"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(COUNT_OF_THREADS, rLockReleasedJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());
    }

    @Test
    public void testBlockLockException() {
        Thread [] threads = new Thread[COUNT_OF_THREADS];
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        for (int i = 0; i < threads.length; i++) {
            int finalI = i % 3;
            threads[i] = new Thread(() -> {
                observation.scoped(() -> {
                    try {
                        testBean.testBlockLockException(String.valueOf(finalI));
                    } catch (Exception e) {
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                //ignore
            }
        }
        jfrEvents.awaitEvents();

        List<RecordedEvent> rLockAcquiredJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockAcquiredJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testBlockLockException:")
        ).toList();
        List<RecordedEvent> rLockReleasedJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockReleasedJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testBlockLockException:")
        ).toList();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.size());
        for (RecordedEvent recordedEvent : rLockAcquiredJFREvents) {
            assertFalse(recordedEvent.getBoolean("tryAcquire"));
            assertEquals(-1, recordedEvent.getLong("waitTime"));
            assertEquals(-1, recordedEvent.getLong("leaseTime"));
            assertEquals(recordedEvent.getString("timeUnit"), "MILLISECONDS");
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertTrue(recordedEvent.getBoolean("lockAcquiredSuccessfully"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());

        assertEquals(COUNT_OF_THREADS, rLockReleasedJFREvents.size());
        for (RecordedEvent recordedEvent : rLockReleasedJFREvents) {
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertTrue(recordedEvent.getBoolean("lockReleasedSuccessfully"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(COUNT_OF_THREADS, rLockReleasedJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());
    }

    @Test
    public void testTryLockNormal() {
        Thread [] threads = new Thread[COUNT_OF_THREADS];
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        for (int i = 0; i < threads.length; i++) {
            int finalI = i % 3;
            threads[i] = new Thread(() -> {
                observation.scoped(() -> {
                    try {
                        testBean.testTryLock(String.valueOf(finalI));
                    } catch (InterruptedException e) {
                        //ignore
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                //ignore
            }
        }
        jfrEvents.awaitEvents();

        List<RecordedEvent> rLockAcquiredJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockAcquiredJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testTryLock:")
        ).toList();
        List<RecordedEvent> rLockReleasedJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockReleasedJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testTryLock:")
        ).toList();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.size());
        for (RecordedEvent recordedEvent : rLockAcquiredJFREvents) {
            assertTrue(recordedEvent.getBoolean("tryAcquire"));
            assertEquals(2000, recordedEvent.getLong("waitTime"));
            assertEquals(-1, recordedEvent.getLong("leaseTime"));
            assertEquals(recordedEvent.getString("timeUnit"), "MILLISECONDS");
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertTrue(recordedEvent.getBoolean("lockAcquiredSuccessfully"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());

        assertEquals(COUNT_OF_THREADS, rLockReleasedJFREvents.size());
        for (RecordedEvent recordedEvent : rLockReleasedJFREvents) {
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertTrue(recordedEvent.getBoolean("lockReleasedSuccessfully"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(COUNT_OF_THREADS, rLockReleasedJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());
    }

    @Test
    public void testTryLockWaitTimeOut() {
        Thread [] threads = new Thread[COUNT_OF_THREADS];
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        for (int i = 0; i < threads.length; i++) {
            int finalI = i % 3;
            threads[i] = new Thread(() -> {
                observation.scoped(() -> {
                    try {
                        testBean.testTryLockWaitTimeOut(String.valueOf(finalI));
                    } catch (InterruptedException e) {
                        //ignore
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                //ignore
            }
        }
        jfrEvents.awaitEvents();

        List<RecordedEvent> rLockAcquiredJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockAcquiredJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testTryLockWaitTimeOut:")
        ).toList();
        List<RecordedEvent> rLockReleasedJFREvents = jfrEvents.events().filter(recordedEvent ->
                recordedEvent.getEventType().getName()
                        .equals("io.github.opensabe.common.redisson.jfr.RLockReleasedJFREvent")
        ).filter(recordedEvent ->
                recordedEvent.getString("lockName").startsWith("TestRedissonLockJFR:testTryLockWaitTimeOut:")
        ).toList();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.size());
        for (RecordedEvent recordedEvent : rLockAcquiredJFREvents) {
            assertTrue(recordedEvent.getBoolean("tryAcquire"));
            assertEquals(10, recordedEvent.getLong("waitTime"));
            assertEquals(-1, recordedEvent.getLong("leaseTime"));
            assertEquals(recordedEvent.getString("timeUnit"), "MILLISECONDS");
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(COUNT_OF_THREADS, rLockAcquiredJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());
        //both success and failure
        Map<Boolean, List<RecordedEvent>> lockAcquiredSuccessfully = rLockAcquiredJFREvents.stream().collect(Collectors.groupingBy(recordedEvent -> recordedEvent.getBoolean("lockAcquiredSuccessfully")));
        assertEquals(2, lockAcquiredSuccessfully.size());

        assertEquals(lockAcquiredSuccessfully.get(true).size(), rLockReleasedJFREvents.size());
        for (RecordedEvent recordedEvent : rLockReleasedJFREvents) {
            assertEquals(recordedEvent.getString("lockClass"), "RedissonLock");
            assertTrue(recordedEvent.getBoolean("lockReleasedSuccessfully"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        assertEquals(lockAcquiredSuccessfully.get(true).size(), rLockReleasedJFREvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());
    }
}
