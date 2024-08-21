package io.github.opensabe.common.redisson.test.jfr;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestRedissonLockJFR.Config.class)
@Execution(ExecutionMode.SAME_THREAD)
public class TestRedissonLockJFR extends BaseRedissonTest {
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

    public static class Config {
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
                    } catch (Exception e) {
                        //ignore
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (Exception e) {
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
            } catch (Exception e) {
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
                    } catch (Exception e) {
                        //ignore
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (Exception e) {
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
                    } catch (Exception e) {
                        //ignore
                    }
                });
            });
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (Exception e) {
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
