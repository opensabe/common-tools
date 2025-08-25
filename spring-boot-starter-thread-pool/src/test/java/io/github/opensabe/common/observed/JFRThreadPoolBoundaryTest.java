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
package io.github.opensabe.common.observed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.scheduler.ThreadPoolStatScheduler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;

@JfrEventTest
@ActiveProfiles("jfr")
@AutoConfigureObservability
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
})
//JFR 测试最好在本地做
@Disabled
@DisplayName("JFR线程池边界测试")
public class JFRThreadPoolBoundaryTest {
    private static final String threadNamePrefix = "threadPoolStat";
    private static final String JFR_EVENT_NAME = ".ThreadTaskJFREvent";
    private static final long UNIT = 500L;
    public JfrEvents jfrEvents = new JfrEvents();
    Logger logger = LogManager.getLogger(JFRThreadPoolBoundaryTest.class);
    @Autowired
    ThreadPoolFactory threadPoolFactory;
    @Autowired
    ThreadPoolStatScheduler scheduler;
    @Autowired
    UnifiedObservationFactory unifiedObservationFactory;

    private static int roundHalfUp(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    @Test
    @DisplayName("测试正常Callable任务 - 验证JFR事件记录和链路追踪")
    public void testNormal() {
        jfrEvents.reset();
        String threadPrefix = threadNamePrefix + "testNormal";
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadPrefix, 2);
        Observation currentOrCreateEmptyObservation1 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentOrCreateEmptyObservation1);
        String traceIdSpinId = traceContext.traceId() + "|" + traceContext.spanId();
        logger.info("TheadPoolStatTest-testNormal traceId {},spanId {} ", traceContext.traceId(), traceContext.spanId());
        currentOrCreateEmptyObservation1.scoped(() -> {
            try {
                Future<Integer> futureOne = null;
                Future<Integer> futureTwo = null;
                Future<Integer> futureThree = null;
                int oneResult = 0;
                int twoResult = 0;
                int threeResult = 0;

                Callable<Integer> callable1 = () -> {
                    logger.info("task1 run...");
                    sleep(UNIT * 3);
                    logger.info("task1 down...");
                    return 1;
                };

                Callable<Integer> callable2 = () -> {
                    logger.info("task2 run...");
                    sleep(UNIT * 4);
                    logger.info("task2 down...");
                    return 2;
                };

                Callable<Integer> callAble3 = () -> {
                    logger.info("task3 run...");
                    sleep(UNIT * 2);
                    logger.info("task3 down...");
                    return 3;
                };

                //执行第一个callableWrapper
                futureOne = executorService.submit(callable1);
                //执行第二个callableWrapper
                futureTwo = executorService.submit(callable2);
                //第三个callable
                futureThree = executorService.submit(callAble3);
                //获取callable1 的值
                oneResult = futureOne.get();
                //获取callable2 的值
                twoResult = futureTwo.get();
                //获取callable3 的值
                threeResult = futureThree.get();
            } catch (Throwable throwable) {
                logger.error("JFRThreadPoolBoundaryTest-testNormal {} ", throwable.getMessage(), throwable);
            }
        });
        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .filter(e -> e.getThread().getJavaName().contains(threadPrefix))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());
        assertEquals(3, events.size());

        Map<String, Long> mapDuration = new HashMap();
        Map<String, Long> map = new HashMap();

        for (int i = 0; i < events.size(); i++) {
            RecordedEvent recordedEvent = events.get(i);
            if (recordedEvent.getThread().getJavaName().contains(threadNamePrefix)) {
                map.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskRunTimeDuration"));
                mapDuration.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskQueueTimeDuration"));
                logger.info("JFRThreadPoolBoundaryTest-testNormal event {} ", recordedEvent);
                assertEquals(traceIdSpinId, recordedEvent.getValue("traceId") + "|" + recordedEvent.getValue("spanId"));
            }
        }
        logger.info("JFRThreadPoolBoundaryTest-testNormal map {}", map);
        logger.info("JFRThreadPoolBoundaryTest-testNormal mapDuration {}", mapDuration);

        //验证等待时间
        assertTrue(
                mapDuration.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 2
        );

        //验证运行时间
        assertTrue(
                map.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 9
        );
    }

    /**
     * JFR 值测试 duration runnable
     */
    @Test
    @DisplayName("测试Runnable任务持续时间 - 验证JFR事件记录")
    public void testDurationRunnable() throws InterruptedException {
        jfrEvents.reset();
        String threadPrefix = threadNamePrefix + "testDurationRunnable";
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadPrefix, 2);

        Observation currentOrCreateEmptyObservation1 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentOrCreateEmptyObservation1);
        String traceIdSpinId = traceContext.traceId() + "|" + traceContext.spanId();
        logger.info("JFRThreadPoolBoundaryTest-testDurationRunnable traceId {},spanId {} ", traceContext.traceId(), traceContext.spanId());

        currentOrCreateEmptyObservation1.scoped(() -> {
            //创建三个可被执行的runnable
            executorService.execute(() -> {
                logger.info("task1 run...");
                try {
                    sleep(UNIT * 2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("task1 down...");
            });

            executorService.execute(() -> {
                logger.info("task2 run...");
                try {
                    sleep(UNIT * 3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("task2 down...");
            });

            executorService.execute(() -> {
                logger.info("task3 run...");
                try {
                    sleep(UNIT * 2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("task3 down...");
            });
        });

        executorService.awaitTermination(UNIT * 10, TimeUnit.MILLISECONDS);

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .filter(e -> e.getThread().getJavaName().contains(threadPrefix))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());
        ;
        assertEquals(3, events.size());

        Map<String, Long> mapDuration = new HashMap();
        Map<String, Long> map = new HashMap();

        for (int i = 0; i < events.size(); i++) {
            RecordedEvent recordedEvent = events.get(i);
            if (recordedEvent.getThread().getJavaName().contains(threadNamePrefix)) {
                map.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskRunTimeDuration"));
                mapDuration.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskQueueTimeDuration"));
                logger.info("JFRThreadPoolBoundaryTest-testNormal event {} ", recordedEvent);
                assertEquals(traceIdSpinId, recordedEvent.getValue("traceId") + "|" + recordedEvent.getValue("spanId"));
            }
        }
        logger.info("JFRThreadPoolBoundaryTest-testNormal map {}", map);
        logger.info("JFRThreadPoolBoundaryTest-testNormal mapDuration {}", mapDuration);
        //验证等待时间
        assertTrue(
                mapDuration.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 1
        );

        //验证运行时间
        assertTrue(
                map.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 5
        );
    }

    /**
     * 测试3  JFR 值测试  exception  callable duration
     */
    @Test
    @DisplayName("测试Callable任务异常处理 - 验证JFR事件记录")
    public void testCallableDurationException() throws InterruptedException, ExecutionException {
        jfrEvents.reset();
        String threadPrefix = threadNamePrefix + "testCallableDurationException";
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadPrefix, 2);
        Future<Integer> futureOne = null;
        Future<Integer> futureTwo = null;
        Future<Integer> futureThree = null;
        int oneResult = 0;
        int twoResult = 0;
        int threeResult = 0;

        Callable<Integer> callable1 = () -> {
            logger.info("task1 run...");
            sleep(UNIT * 1);
            int a = 9;
            int b = 0;
            int c = a / b;
            logger.info("task1 down...");
            return 1;
        };

        Callable<Integer> callable2 = () -> {
            logger.info("task2 run...");
            sleep(UNIT * 4);
            logger.info("task2 down...");
            return 2;
        };

        Callable<Integer> callAble3 = () -> {
            logger.info("task3 run...");
            sleep(UNIT * 4);
            logger.info("task3 down...");
            return 3;
        };

        //执行第一个callableWrapper
        futureOne = executorService.submit(callable1);
        //执行第二个callableWrapper
        futureTwo = executorService.submit(callable2);
        //sleep 之后不会等待
        TimeUnit.MILLISECONDS.sleep(2 * UNIT);

        //第三个callable  等待了2秒  因为callable自己throw exception
        futureThree = executorService.submit(callAble3);
        //        //获取callable1 的值
        try {
            oneResult = futureOne.get();
        } catch (Throwable e) {
            logger.info("there is some exception happening " + e);
        }
        //获取callable2 的值
        twoResult = futureTwo.get();
        //获取callable3 的值
        threeResult = futureThree.get();

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .filter(e -> e.getThread().getJavaName().contains(threadPrefix))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());
        ;
        assertEquals(3, events.size());

        Map<String, Long> mapDuration = new HashMap();
        Map<String, Long> map = new HashMap();

        for (int i = 0; i < events.size(); i++) {
            RecordedEvent recordedEvent = events.get(i);
            if (recordedEvent.getThread().getJavaName().contains(threadNamePrefix)) {
                map.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskRunTimeDuration"));
                mapDuration.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskQueueTimeDuration"));
                logger.info("JFRThreadPoolBoundaryTest-testNormal event {} ", recordedEvent);
            }
        }
        logger.info("JFRThreadPoolBoundaryTest-testNormal map {}", map);
        logger.info("JFRThreadPoolBoundaryTest-testNormal mapDuration {}", mapDuration);

        //验证等待时间
        assertTrue(
                mapDuration.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) == 0
        );

        //验证运行时间
        assertTrue(
                map.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 5
        );
    }

    /**
     * JFR 值测试4  exception  runnbale duration
     * 线程池 线程执行任务1时会异常退出(线程一退出)，执行任务2时创建新线程
     */
    @Test
    @DisplayName("测试Runnable任务异常处理 - 验证JFR事件记录")
    public void testRunnableDurationException() throws InterruptedException {
        jfrEvents.reset();
        String threadPrefix = threadNamePrefix + "testRunnableDurationException";
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadPrefix, 1);
        executorService.execute(() -> {
            logger.info("task1 run...");
            try {
                sleep(UNIT * 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int a = 9;
            int b = 0;
            int c = a / b;
            logger.info("task1 down...");
        });

        executorService.execute(() -> {
            logger.info("task2 run...");
            try {
                sleep(UNIT * 2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("task2 down...");
        });

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .filter(e -> e.getThread().getJavaName().contains(threadPrefix))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());
        ;
        assertEquals(2, events.size());

        Map<String, Long> mapDuration = new HashMap();
        Map<String, Long> map = new HashMap();

        for (int i = 0; i < events.size(); i++) {
            RecordedEvent recordedEvent = events.get(i);
            if (recordedEvent.getThread().getJavaName().contains(threadNamePrefix)) {
                map.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskRunTimeDuration"));
                mapDuration.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskQueueTimeDuration"));
                logger.info("JFRThreadPoolBoundaryTest-testNormal event {} ", recordedEvent);
            }
        }
        logger.info("JFRThreadPoolBoundaryTest-testNormal map {}", map);
        logger.info("JFRThreadPoolBoundaryTest-testNormal mapDuration {}", mapDuration);
        //验证等待时间
        assertTrue(
                mapDuration.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 1
        );

        //验证运行时间
        assertTrue(
                map.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 3
        );

    }

    /**
     * 测试5  JFR 值测试  throwable  callable duration
     */
    @Test
    @DisplayName("测试Callable任务异常处理 - 验证JFR事件记录")
    public void testCallableDurationThrowable() throws InterruptedException, ExecutionException {
        jfrEvents.reset();
        String threadPrefix = threadNamePrefix + "testCallableDurationThrowable";
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadPrefix, 2);
        //创建三个可被执行的callable
        Future<Integer> futureOne = null;
        Future<Integer> futureTwo = null;
        Future<Integer> futureThree = null;
        int oneResult = 0;
        int twoResult = 0;
        int threeResult = 0;

        Callable<Integer> callable1 = () -> {
            logger.info("task1 run...");
            sleep(UNIT * 3);
            int a = 9;
            int b = 0;
            int c = a / b;
            logger.info("task1 down...");
            return 1;
        };

        Callable<Integer> callable2 = () -> {
            logger.info("task2 run...");
            sleep(UNIT * 4);
            logger.info("task2 down...");
            return 2;
        };

        Callable<Integer> callAble3 = () -> {
            logger.info("task3 run...");
            sleep(UNIT * 2);
            logger.info("task3 down...");
            return 3;
        };

        //执行第一个callableWrapper
        futureOne = executorService.submit(callable1);
        //执行第二个callableWrapper
        futureTwo = executorService.submit(callable2);

        TimeUnit.MILLISECONDS.sleep(UNIT * 1);

        futureThree = executorService.submit(callAble3);
        //        //获取callable1 的值
        try {
            oneResult = futureOne.get();
        } catch (Throwable e) {
            logger.info("there is some exception happening " + e);
        }
        //获取callable2 的值
        twoResult = futureTwo.get();
        //获取callable3 的值
        threeResult = futureThree.get();

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .filter(e -> e.getThread().getJavaName().contains(threadPrefix))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());
        ;
        assertEquals(3, events.size());

        Map<String, Long> mapDuration = new HashMap();
        Map<String, Long> map = new HashMap();

        for (int i = 0; i < events.size(); i++) {
            RecordedEvent recordedEvent = events.get(i);
            if (recordedEvent.getThread().getJavaName().contains(threadNamePrefix)) {
                map.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskRunTimeDuration"));
                mapDuration.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskQueueTimeDuration"));
                logger.info("JFRThreadPoolBoundaryTest-testNormal event {} ", recordedEvent);
            }
        }
        logger.info("JFRThreadPoolBoundaryTest-testNormal map {}", map);
        logger.info("JFRThreadPoolBoundaryTest-testNormal mapDuration {}", mapDuration);
        //验证等待时间
        assertTrue(
                mapDuration.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 2
        );

        //验证运行时间
        assertTrue(
                map.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 9
        );

    }

    /**
     * 测试6  JFR 值测试  throwable  runnable duration
     */
    @Test
    @DisplayName("测试Runnable任务异常处理 - 验证JFR事件记录")
    public void testRunnableDurationThrowable() throws InterruptedException, ExecutionException {
        jfrEvents.reset();
        String threadPrefix = threadNamePrefix + "testRunnableDurationThrowable";
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadPrefix, 1);
        try {
            executorService.execute(() -> {
                logger.info("task1 run...");
                try {
                    sleep(UNIT * 3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                int a = 9;
                int b = 0;
                int c = a / b;
                logger.info("task1 down...");
            });
        } catch (Throwable throwable) {
            logger.error("there is some exception happening {}", throwable);
        }

        executorService.execute(() -> {
            logger.info("task2 run...");
            try {
                sleep(UNIT * 4);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("task2 down...");
        });

        executorService.awaitTermination(UNIT * 10, TimeUnit.MILLISECONDS);
        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .filter(e -> e.getThread().getJavaName().contains(threadPrefix))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());
        ;
        assertEquals(2, events.size());

        Map<String, Long> mapDuration = new HashMap();
        Map<String, Long> map = new HashMap();

        for (int i = 0; i < events.size(); i++) {
            RecordedEvent recordedEvent = events.get(i);
            if (recordedEvent.getThread().getJavaName().contains(threadNamePrefix)) {
                map.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskRunTimeDuration"));
                mapDuration.put(recordedEvent.getThread().getJavaName() + "-" + i, recordedEvent.getValue("taskQueueTimeDuration"));
                logger.info("JFRThreadPoolBoundaryTest-testNormal event {} ", recordedEvent);
            }
        }
        logger.info("JFRThreadPoolBoundaryTest-testNormal map {}", map);
        logger.info("JFRThreadPoolBoundaryTest-testNormal mapDuration {}", mapDuration);
        //验证等待时间
        assertTrue(
                mapDuration.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 3
        );

        //验证运行时间
        assertTrue(
                map.values().stream()
                        .map(v -> roundHalfUp((double) v / UNIT))
                        .reduce(0, Integer::sum) >= 7
        );

    }

    @SpringBootApplication
    static class MockConfig {
    }
}