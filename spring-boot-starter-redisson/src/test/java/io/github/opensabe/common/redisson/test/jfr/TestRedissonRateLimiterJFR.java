package io.github.opensabe.common.redisson.test.jfr;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
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
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class TestRedissonRateLimiterJFR {
    private static final int THREAD_COUNT = 10;
    public JfrEvents jfrEvents = new JfrEvents();

    @EnableAutoConfiguration
    @Configuration
    public static class App {
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
                rateIntervalUnit = RateIntervalUnit.SECONDS
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
                    } catch (InterruptedException e) {
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
