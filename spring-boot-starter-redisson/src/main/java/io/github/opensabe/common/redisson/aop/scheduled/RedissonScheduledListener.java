package io.github.opensabe.common.redisson.aop.scheduled;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.annotation.RedissonScheduled;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Log4j2
public class RedissonScheduledListener {

    private final RedissonScheduledBeanPostProcessor processor;
    private final UnifiedObservationFactory unifiedObservationFactory;
    private final RedissonClient redissonClient;
    private final MeterRegistry meterRegistry;

    private final Map<String, ExecutorWrapper> map = Maps.newConcurrentMap();

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public RedissonScheduledListener(RedissonScheduledBeanPostProcessor processor, UnifiedObservationFactory unifiedObservationFactory, RedissonClient redissonClient, MeterRegistry meterRegistry) {
        this.processor = processor;
        this.unifiedObservationFactory = unifiedObservationFactory;
        this.redissonClient = redissonClient;
        this.meterRegistry = meterRegistry;
    }


    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        if (initialized.get()) {
            return;
        }
        processor.getBeanMap().forEach((beanName, bean) -> {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
            for (Method method : targetClass.getMethods()) {
                RedissonScheduled annotation = method.getAnnotation(RedissonScheduled.class);
                if (annotation != null) {
                    String name = annotation.name();
                    if (StringUtils.isBlank(name)) {
                        name = targetClass.getSimpleName() + "#" + method.getName();
                    }
                    if (map.containsKey(name)) {
                        throw new BeanCreationException("RedissonScheduled name duplicated");
                    } else {
                        map.put(name, new ExecutorWrapper(redissonClient, unifiedObservationFactory, () -> method.invoke(bean)
                           , name, annotation.initialDelay(),
                                annotation.fixedDelay(), annotation.stopOnceShutdown(), meterRegistry));
                    }

                }
            }
            if (bean instanceof RedissonScheduledService scheduledService) {
//                Method method;
//                try {
//                    method = RedissonScheduledService.class.getDeclaredMethod("run");
//                } catch (NoSuchMethodException e) {
//                    throw new RuntimeException(e);
//                }
                map.put(scheduledService.name(), new ExecutorWrapper(redissonClient, unifiedObservationFactory,
                        scheduledService, scheduledService.name(), scheduledService.initialDelay(),
                        scheduledService.fixedDelay(), scheduledService.stopOnceShutdown(), meterRegistry));
            }
        });
        initialized.set(true);
    }

    public void close() {
        log.info("closing RedissonScheduledListener...");
        map.values().parallelStream().forEach(ExecutorWrapper::close);
        log.info("RedissonScheduledListener closed...");
    }

    private static class ExecutorWrapper {
        private final Thread leaderLatch;
        private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
        private volatile boolean isLeader;
        private volatile boolean isStopped = false;
        private  final boolean stopOnceShutdown;

        private final DistributionSummary distributionSummary;

        public ExecutorWrapper(RedissonClient redissonClient, UnifiedObservationFactory unifiedObservationFactory,
                               ScheduledService runnable,
                               String name, long initialDelay,
                               long fixedDelay, boolean stopOnceShutdown, MeterRegistry meterRegistry) {
            RLock lock = redissonClient.getLock(name + ":leader");
            this.stopOnceShutdown = stopOnceShutdown;
            ThreadFactory build = new ThreadFactoryBuilder().setNameFormat(name + "_scheduler").build();
            this.distributionSummary = DistributionSummary
                    .builder("redisson.schedule.task." + name)
                    .distributionStatisticBufferLength(20)
                    .distributionStatisticExpiry(Duration.ofDays(30))
                    .publishPercentileHistogram(Boolean.TRUE)
                    .publishPercentiles(0.1, 0.5, 0.9)
                    .register(meterRegistry);
            //
            Observation observation = unifiedObservationFactory.createEmptyObservation();
            leaderLatch = new Thread(() -> {
                lock.lock();
                while (!isStopped) {
                    try {
                        if (lock.isHeldByCurrentThread()) {
                            isLeader = true;
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException ignore) {
                            }
                        } else {
                            isLeader = false;
                            lock.lock();
                        }
                    } catch (RedissonShutdownException e) {
                        log.warn("ExecutorWrapper-ExecutorWrapper loop stops because redisson is shutdown (probably restart happens)!", e);
                    } catch (Throwable e) {
                        log.fatal("ExecutorWrapper-ExecutorWrapper loop error: {}", e.getMessage(), e);
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
            }, name + "_latch");
            leaderLatch.start();

            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, build, new ThreadPoolExecutor.AbortPolicy());
            scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> observation.observe(() -> {
                try {
                    if (isLeader) {
                        long start = System.currentTimeMillis();
                        if (log.isDebugEnabled()) {
                            log.debug("RedissonScheduledBeanPostProcessor task: {} start", name);
                        }
//                        method.invoke(bean);
                        runnable.run();
                        long elapsed = System.currentTimeMillis() - start;
                        if (distributionSummary.count() > 10 && (elapsed > distributionSummary.max() * 2) && elapsed > 60000) {
                            log.fatal("RedissonScheduledBeanPostProcessor task: {} end in {} ms, recent mean elapsed time is {}ms", name, elapsed, distributionSummary.mean());
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("RedissonScheduledBeanPostProcessor task: {} end in {} ms", name, elapsed);
                            }
                        }
                        distributionSummary.record(elapsed);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("RedissonScheduledBeanPostProcessor not leader, ignore task: {}", name);
                        }
                    }
                } catch (Throwable e) {
                    log.fatal("RedissonScheduledBeanPostProcessor task: {}, error: {}", name, e.getMessage(), e);
                }
            }), initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
        }

        void close() {
            log.info("closing RedissonScheduledBeanPostProcessor executor {} ...", scheduledThreadPoolExecutor.toString());
            isStopped = true;
            leaderLatch.interrupt();
            if (stopOnceShutdown) {
                scheduledThreadPoolExecutor.shutdownNow();
            } else {
                scheduledThreadPoolExecutor.shutdown();
                try {
                    scheduledThreadPoolExecutor.awaitTermination(50, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.warn("interrupted while waiting in closing RedissonScheduledBeanPostProcessor", e);
                }
            }
            log.info("RedissonScheduledBeanPostProcessor executor {} closed...", scheduledThreadPoolExecutor.toString());
        }
    }


}
