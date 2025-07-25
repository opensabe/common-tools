package io.github.opensabe.common.redisson.test.jfr;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.redisson.api.RateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.SAME_THREAD)
@Import(TestRedissonRateLimiterJFR.Config.class)
//JFR 测试最好在本地做
@Disabled
@JfrEventTest
public class TestRedissonRateLimiterJFR extends BaseRedissonTest {
    private static final int THREAD_COUNT = 10;
    public JfrEvents jfrEvents = new JfrEvents();

    public static class Config {
        @Bean
        public TestRedissonRateLimiterBean testRedissonRateLimiterBean() {
            return new TestRedissonRateLimiterBean();
        }
    }

    public static class TestRedissonRateLimiterBean {

        @RedissonRateLimiter(
                name = "TestRedissonRateLimiterJFR-testRateLimiterTryAcquireWithWait",
                type = RedissonRateLimiter.Type.TRY,
                waitTime = THREAD_COUNT / 2,
                timeUnit = TimeUnit.SECONDS,
                rate = 1,
                rateInterval = 1,
                rateType = RateType.OVERALL,
                rateIntervalUnit = TimeUnit.SECONDS
        )
        public void testRateLimiterTryAcquireWithWait() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    @Autowired
    private TestRedissonRateLimiterBean testRedissonRateLimiterBean;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Test
    public void testRateLimiterTryAcquireWithWait() throws InterruptedException {
        Thread []threads = new Thread[THREAD_COUNT];
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> {
                observation.scoped(() -> {
                    try {
                        testRedissonRateLimiterBean.testRateLimiterTryAcquireWithWait();
                    } catch (Exception e) {
                        //ignore
                    }
                });
            });
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        jfrEvents.awaitEvents();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        List<RecordedEvent> recordedEvents = jfrEvents.events()
                .filter(recordedEvent -> recordedEvent.getEventType().getName().equals("io.github.opensabe.common.redisson.jfr.RRateLimiterAcquireJFREvent"))
                .filter(recordedEvent -> recordedEvent.getString("rateLimiterName").equals("TestRedissonRateLimiterJFR-testRateLimiterTryAcquireWithWait"))
                .collect(Collectors.toList());
        assertEquals(THREAD_COUNT, recordedEvents.size());
        for (RecordedEvent recordedEvent : recordedEvents) {
            assertEquals(5, recordedEvent.getLong("timeout"));
            assertEquals(recordedEvent.getString("timeUnit"), "SECONDS");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
        }
        Map<Boolean, List<RecordedEvent>> acquireSuccessfully = recordedEvents.stream().collect(
                Collectors.groupingBy(recordedEvent -> recordedEvent.getBoolean("acquireSuccessfully"))
        );
        assertEquals(2, acquireSuccessfully.size());
        assertEquals(THREAD_COUNT, recordedEvents.stream().map(recordedEvent -> recordedEvent.getString("spanId")).distinct().count());
    }
}
