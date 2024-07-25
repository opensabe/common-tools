package io.github.opensabe.common.observed;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.scheduler.ThreadPoolStatScheduler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@JfrEventTest
@ActiveProfiles("jfr")
@AutoConfigureObservability
@SpringBootTest(properties = {"eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"}
)
public class JFRThreadPoolBoundaryTest {
    Logger logger = LogManager.getLogger(JFRThreadPoolBoundaryTest.class);
    private static final String threadNamePrefix = "threadPoolStat";
    
    private static final String JFR_EVENT_NAME = ".ThreadTaskJFREvent";

    @SpringBootApplication
    static class MockConfig {
    }

    @Autowired
    ThreadPoolFactory threadPoolFactory;
    @Autowired
    ThreadPoolStatScheduler scheduler;
    @Autowired
    UnifiedObservationFactory unifiedObservationFactory;

    public JfrEvents jfrEvents = new JfrEvents();

    @Test
    public void testNormal() {
        jfrEvents.reset();
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadNamePrefix, 2);
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
                    sleep(1000 * 3);
                    logger.info("task1 down...");
                    return 1;
                };

                Callable<Integer> callable2 = () -> {
                    logger.info("task2 run...");
                    sleep(1000 * 4);
                    logger.info("task2 down...");
                    return 2;
                };

                Callable<Integer> callAble3 = () -> {
                    logger.info("task3 run...");
                    sleep(1000 * 2);
                    logger.info("task3 down...");
                    return 3;
                };

                //执行第一个callableWrapper
                futureOne = executorService.submit(callable1);
                //执行第二个callableWrapper
                futureTwo = executorService.submit(callable2);

                TimeUnit.SECONDS.sleep(1);

                //第三个callable  等待了1秒  因为睡了1秒
                futureThree = executorService.submit(callAble3);
                //获取callable1 的值
                oneResult = futureOne.get();
                //获取callable2 的值
                twoResult = futureTwo.get();
                //获取callable3 的值
                threeResult = futureThree.get();

                sleep(1000 * 8);
            } catch (Throwable throwable) {
                logger.error("JFRThreadPoolBoundaryTest-testNormal {} ", throwable.getMessage(), throwable);
            }
        });
        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
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
                        .map(v -> roundHalfUp((double) v / 1000))
                        .reduce(0, Integer::sum).equals(2)
        );

        //验证运行时间
        Set<Integer> collect = map.values().stream()
                .map(v -> roundHalfUp((double) v / 1000))
                .collect(Collectors.toSet());
        assertTrue(collect.contains(3));
        assertTrue(collect.contains(4));
        assertTrue(collect.contains(2));
    }

    /**
     * JFR 值测试 duration runnable
     */
    @Test
    public void testDurationRunnable() throws InterruptedException {
        jfrEvents.reset();
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadNamePrefix, 2);

        Observation currentOrCreateEmptyObservation1 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentOrCreateEmptyObservation1);
        String traceIdSpinId = traceContext.traceId() + "|" + traceContext.spanId();
        logger.info("JFRThreadPoolBoundaryTest-testDurationRunnable traceId {},spanId {} ", traceContext.traceId(), traceContext.spanId());

        currentOrCreateEmptyObservation1.scoped(() -> {
            //创建三个可被执行的runnable
            executorService.execute(() -> {
                logger.info("task1 run...");
                try {
                    sleep(1000 * 2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("task1 down...");
            });

            executorService.execute(() -> {
                logger.info("task2 run...");
                try {
                    sleep(1000 * 3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("task2 down...");
            });

            try {
                sleep(1000 * 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            executorService.execute(() -> {
                logger.info("task3 run...");
                try {
                    sleep(1000 * 2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info("task3 down...");
            });
        });

        sleep(1000 * 6);

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());;
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
                        .map(v -> roundHalfUp((double) v / 1000))
                        .reduce(0, Integer::sum).equals(1)
        );

        //验证运行时间
        Set<Integer> collect = map.values().stream()
                .map(v -> roundHalfUp((double) v / 1000))
                .collect(Collectors.toSet());
        assertTrue(collect.contains(3));
        assertTrue(collect.contains(2));
    }

    /**
     * 测试3  JFR 值测试  exception  callable duration
     */
    @Test
    public void testCallableDurationException() throws InterruptedException, ExecutionException {
        jfrEvents.reset();
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadNamePrefix, 2);
        Future<Integer> futureOne = null;
        Future<Integer> futureTwo = null;
        Future<Integer> futureThree = null;
        int oneResult = 0;
        int twoResult = 0;
        int threeResult = 0;

        Callable<Integer> callable1 = () -> {
            logger.info("task1 run...");
            sleep(1000 * 1);
            int a = 9;
            int b = 0;
            int c = a / b;
            logger.info("task1 down...");
            return 1;
        };

        Callable<Integer> callable2 = () -> {
            logger.info("task2 run...");
            sleep(1000 * 4);
            logger.info("task2 down...");
            return 2;
        };

        Callable<Integer> callAble3 = () -> {
            logger.info("task3 run...");
            sleep(1000 * 4);
            logger.info("task3 down...");
            return 3;
        };

        //执行第一个callableWrapper
        futureOne = executorService.submit(callable1);
        //执行第二个callableWrapper
        futureTwo = executorService.submit(callable2);

        TimeUnit.SECONDS.sleep(1);

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

        sleep(1000 * 10);

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());;
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
                        .map(v -> roundHalfUp((double) v / 1000))
                        .reduce(0, Integer::sum).equals(0)
        );

        //验证运行时间
        Set<Integer> collect = map.values().stream()
                .map(v -> roundHalfUp((double) v / 1000))
                .collect(Collectors.toSet());
        assertTrue(collect.contains(1));
        assertTrue(collect.contains(4));

    }

    /**
     * JFR 值测试4  exception  runnbale duration
     * 线程池 线程执行任务1时会异常退出(线程一退出)，执行任务2时创建新线程
     */
    @Test
    public void testRunnableDurationException() throws InterruptedException {
        jfrEvents.reset();
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadNamePrefix, 1);
        executorService.execute(() -> {
            logger.info("task1 run...");
            try {
                sleep(1000 * 1);
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
                sleep(1000 * 2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("task2 down...");
        });
        sleep(1000 * 5);

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());;
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
                        .map(v -> roundHalfUp((double) v / 1000))
                        .reduce(0, Integer::sum).equals(1)
        );

        //验证运行时间
        Set<Integer> collect = map.values().stream()
                .map(v -> roundHalfUp((double) v / 1000))
                .collect(Collectors.toSet());
        assertTrue(collect.contains(1));
        assertTrue(collect.contains(2));

    }

    /**
     * 测试5  JFR 值测试  throwable  callable duration
     */
    @Test
    public void testCallableDurationThrowable() throws InterruptedException, ExecutionException {
        jfrEvents.reset();
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadNamePrefix, 2);
        //创建三个可被执行的callable
        Future<Integer> futureOne = null;
        Future<Integer> futureTwo = null;
        Future<Integer> futureThree = null;
        int oneResult = 0;
        int twoResult = 0;
        int threeResult = 0;

        Callable<Integer> callable1 = () -> {
            logger.info("task1 run...");
            sleep(1000 * 3);
            int a = 9;
            int b = 0;
            int c = a / b;
            logger.info("task1 down...");
            return 1;
        };

        Callable<Integer> callable2 = () -> {
            logger.info("task2 run...");
            sleep(1000 * 4);
            logger.info("task2 down...");
            return 2;
        };

        Callable<Integer> callAble3 = () -> {
            logger.info("task3 run...");
            sleep(1000 * 2);
            logger.info("task3 down...");
            return 3;
        };

        //执行第一个callableWrapper
        futureOne = executorService.submit(callable1);
        //执行第二个callableWrapper
        futureTwo = executorService.submit(callable2);

        TimeUnit.SECONDS.sleep(1);

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

        sleep(1000 * 9);

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());;
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
                        .map(v -> roundHalfUp((double) v / 1000))
                        .reduce(0, Integer::sum).equals(2)
        );

        //验证运行时间
        Set<Integer> collect = map.values().stream()
                .map(v -> roundHalfUp((double) v / 1000))
                .collect(Collectors.toSet());
        assertTrue(collect.contains(3));
        assertTrue(collect.contains(4));
        assertTrue(collect.contains(2));

    }

    /**
     * 测试6  JFR 值测试  throwable  runnable duration
     */
    @Test
    public void testRunnableDurationThrowable() throws InterruptedException, ExecutionException {
        jfrEvents.reset();
        ExecutorService executorService = threadPoolFactory.createNormalThreadPool(threadNamePrefix, 1);
        try {
            executorService.execute(() -> {
                logger.info("task1 run...");
                try {
                    sleep(1000 * 3);
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
                sleep(1000 * 4);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("task2 down...");
        });

        sleep(1000 * 10);

        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(e -> e.getEventType().getName().contains(JFR_EVENT_NAME))
                .sorted(Comparator.comparing(s -> s.getThread().getJavaName()))
                .collect(Collectors.toList());;
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
                        .map(v -> roundHalfUp((double) v / 1000))
                        .reduce(0, Integer::sum).equals(3)
        );

        //验证运行时间
        Set<Integer> collect = map.values().stream()
                .map(v -> roundHalfUp((double) v / 1000))
                .collect(Collectors.toSet());
        assertTrue(collect.contains(3));
        assertTrue(collect.contains(4));

    }
    
    private static int roundHalfUp(double value) {
        return BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}