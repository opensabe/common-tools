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
package io.github.opensabe.common.utils.buffer;

import com.google.common.collect.Sets;
import io.github.opensabe.common.buffer.BatchBufferedCountDownQueue;
import io.github.opensabe.common.buffer.BatchBufferedQueue;
import io.github.opensabe.common.buffer.BufferedCountDownLatchElement;
import io.github.opensabe.common.buffer.BufferedElement;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.EnableEvent;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@JfrEventTest
@AutoConfigureObservability
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BatchBufferQueueTest.App.class,
        properties = {
                "eureka.client.enabled=false"
        }
)
@DisplayName("批量缓冲队列测试")
public class BatchBufferQueueTest {
    public JfrEvents jfrEvents = new JfrEvents();
    @SpringBootApplication
    public static class App {

        @Log4j2
        @Component
        public static class Queue1 extends BatchBufferedQueue<Event> {

            @Override
            protected Comparator<Event> comparator() {
                return Comparator.comparing(Event::getId);
            }

            @Override
            protected void batchManipulate(List<Event> batch) {
                boolean b =
                        //模拟有错误
                        batch.stream().anyMatch(Event::isHasError)
                        //验证 beforeExecute 已经被执行
                        || batch.stream().anyMatch(event -> !event.beforeExecute)
                        //验证 afterBatchFinish 没有被执行
                        || batch.stream().anyMatch(event -> event.afterBatchFinish);
                if (b) {
                    throw new RuntimeException(ERROR_MSG);
                }
                results.add(batch);
                batch.forEach(e -> {
                    log.info("----------" + e.id);
                    countDownLatch.countDown();
                });
            }

            @Override
            protected int queueCount() {
                return QUEUE_COUNT;
            }

            @Override
            protected int queueSize() {
                return super.queueSize();
            }

            @Override
            protected int batchSize() {
                return super.batchSize();
            }

            @Override
            protected long pollWaitTimeInMillis() {
                return POLL_WAIT_TIME_IN_MILLIS;
            }

            @Override
            protected long maxWaitTimeInMillis() {
                return MAX_WAIT_TIME_IN_MILLIS;
            }

            @Override
            protected void beforeExecute(List<Event> batch) {
                batch.forEach(event -> event.beforeExecute = true);
            }

            @Override
            protected void afterBatchFinish(List<Event> batch) {
                batch.forEach(event -> event.afterBatchFinish = true);
            }

            @Override
            protected void afterBatchError(List<Event> batch, Throwable throwable) {
                batch.forEach(event -> {
                    event.throwable = throwable;
                    countDownLatch.countDown();
                });
            }
        }

        @Component
        public static class Queue2 extends BatchBufferedCountDownQueue<CountDownEvent> {
            @Override
            protected Comparator<CountDownEvent> comparator() {
                return Comparator.comparing(CountDownEvent::getId);
            }

            @Override
            protected void batchManipulate(List<CountDownEvent> batch) {
                batch.forEach(e -> {
                    e.value++;
                });
            }
        }

    }

    @Autowired
    private App.Queue1 queue1;
    @Autowired
    private App.Queue2 queue2;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    private static final int EVENT_COUNT = 150;
    private static final int QUEUE_COUNT = 2;

    private static final long POLL_WAIT_TIME_IN_MILLIS = 10L;
    private static final long MAX_WAIT_TIME_IN_MILLIS = 1000L;
    private static final String ERROR_MSG = "error";

    private static final ConcurrentLinkedQueue<List<Event>> results = new ConcurrentLinkedQueue<>();

    private static final CountDownLatch countDownLatch = new CountDownLatch(EVENT_COUNT);
    @Test
    @DisplayName("测试批量缓冲队列和JFR事件记录 - 验证异步处理和链路追踪")
    @EnableEvent("io.github.opensabe.common.buffer.BufferedElementJFREvent")
    //JFR 测试最好在本地做
    @Disabled
    void testBufferedQueueAndJFR () throws InterruptedException {
        Thread[] threads = new Thread[EVENT_COUNT];
        Set<String> traceIds = Sets.newConcurrentHashSet();
        for (int i = 0; i < EVENT_COUNT / 3; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
                observation.scoped(() -> {
                    queue1.submit(new Event(finalI +""));
                    TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
                    traceIds.add(traceContext.traceId());
                });
            });
            threads[i].start();
        }
        TimeUnit.MILLISECONDS.sleep(MAX_WAIT_TIME_IN_MILLIS);
        for (int i = EVENT_COUNT / 3; i < EVENT_COUNT / 3 * 2 ; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
                observation.scoped(() -> {
                    queue1.submit(new Event(finalI +""));
                    TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
                    traceIds.add(traceContext.traceId());
                });
            });
            threads[i].start();
        }
        TimeUnit.MILLISECONDS.sleep(MAX_WAIT_TIME_IN_MILLIS);
        for (int i = EVENT_COUNT / 3 * 2; i < EVENT_COUNT ; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
                observation.scoped(() -> {
                    Event event = new Event(finalI + "");
                    event.hasError = true;
                    queue1.submit(event);
                    TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
                    traceIds.add(traceContext.traceId());
                });
            });
            threads[i].start();
        }

        for (int i = 0; i < EVENT_COUNT; i++) {
            threads[i].join();
        }
        //需要等待所有事件都被处理完，因为这个是异步队列
        countDownLatch.await();
        jfrEvents.awaitEvents();
        List<RecordedEvent> collect = jfrEvents.events().collect(Collectors.toList());
        assertEquals(EVENT_COUNT, traceIds.size());
        assertEquals(EVENT_COUNT, collect.size());
        assertEquals(traceIds, collect.stream().map(e -> e.getString("submitTraceId")).collect(Collectors.toSet()));
        //验证每一批次的处理 spanId 都是一样的
        Map<String, List<RecordedEvent>> batchSpanId = collect.stream().collect(Collectors.groupingBy(e -> e.getString("batchSpanId")));
        //有批次是错误的，所以批次数量会比 Span 数量少
        assertTrue(results.size()  < batchSpanId.size());

        collect.forEach(recordedEvent -> {
            //验证 queueTime 和 duration 的值是合理的
            assertTrue(recordedEvent.getDuration().toMillis() < 1000);
            assertTrue(recordedEvent.getDuration().toMillis() > 0);
            assertTrue(recordedEvent.getDuration("queueTime").toMillis() < 1000);
            assertTrue(recordedEvent.getDuration("queueTime").toMillis() > 0);
        });
        //验证有错误的事件，错误信息合理
        List<RecordedEvent> errorCollect = collect.stream().filter(e -> e.getString("error") != null).collect(Collectors.toList());
        errorCollect.forEach(e -> {
            assertEquals(ERROR_MSG, e.getString("error"));
        });
        assertEquals(errorCollect.size(), 50);
        //验证所有事件批次里面都是排序好的
        results.forEach(l -> {
            for (int i = 0; i < l.size() - 1; i++) {
                assertTrue(l.get(i).getId().compareTo(l.get(i + 1).getId()) < 0);
            }
        });
        //验证所有事件批次里面都是同一个哈希值取余，这样代表是同一个 queue 处理的
        results.forEach(l -> {
            for (int i = 0; i < l.size() - 1; i++) {
                assertEquals(Math.abs(l.get(i).hashKey().hashCode() % QUEUE_COUNT), Math.abs(l.get(i + 1).hashKey().hashCode() % QUEUE_COUNT));
            }
        });
    }

    @Test
    @DisplayName("测试批量缓冲计数队列 - 验证同步处理")
    public void testBatchBufferedCountDownQueue() throws InterruptedException {
        Thread[] threads = new Thread[EVENT_COUNT];
        CountDownEvent[] countDownEvents = new CountDownEvent[EVENT_COUNT];
        for (int i = 0; i < EVENT_COUNT; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                countDownEvents[finalI] = new CountDownEvent(finalI + "");
                queue2.submit(countDownEvents[finalI]);
            });
            threads[i].start();
        }
        for (int i = 0; i < EVENT_COUNT; i++) {
            threads[i].join();
        }
        //验证都是处理完的，证明 submit 是同步的
        for (int i = 0; i < EVENT_COUNT; i++) {
            assertEquals(countDownEvents[i].value, 1);
        }

    }



    @Getter
    static class Event extends BufferedElement {
        private String id;
        private volatile boolean beforeExecute;
        private volatile boolean afterBatchFinish;
        private volatile boolean hasError;
        private volatile Throwable throwable;
        public Event(String id) {
            this.id = "test" + id;
        }
        //按照 id 的哈希值分组
        @Override
        public String hashKey() {
            return id;
        }
    }

    @Getter
    static class CountDownEvent extends BufferedCountDownLatchElement {
        private String id;
        private volatile int value = 0;
        public CountDownEvent(String id) {
            this.id = "test" + id;
        }
        //按照 id 的哈希值分组
        @Override
        public String hashKey() {
            return id;
        }
    }
}
