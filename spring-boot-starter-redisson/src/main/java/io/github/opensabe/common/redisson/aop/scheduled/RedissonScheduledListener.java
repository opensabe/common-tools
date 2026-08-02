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
package io.github.opensabe.common.redisson.aop.scheduled;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.annotation.RedissonScheduled;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonShutdownException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;


/**
 * 应用启动后注册 Redisson 选主定时任务，并在 RefreshScope 刷新时热更新调度参数。
 */
@Log4j2
public class RedissonScheduledListener {

    /** 待调度 Bean 扫描器。 */
    private final RedissonScheduledBeanPostProcessor processor;

    /** 统一观测工厂。 */
    private final UnifiedObservationFactory unifiedObservationFactory;

    /** Redisson 客户端（选主锁）。 */
    private final RedissonClient redissonClient;

    /** Micrometer 指标注册表。 */
    private final MeterRegistry meterRegistry;

    /** 任务名称 → 执行器包装。 */
    private final Map<String, ExecutorWrapper> map = Maps.newConcurrentMap();


    /**
     * @param processor Bean 扫描器
     * @param unifiedObservationFactory 观测工厂
     * @param redissonClient Redisson 客户端
     * @param meterRegistry 指标注册表
     */
    public RedissonScheduledListener(RedissonScheduledBeanPostProcessor processor, UnifiedObservationFactory unifiedObservationFactory, RedissonClient redissonClient, MeterRegistry meterRegistry) {
        this.processor = processor;
        this.unifiedObservationFactory = unifiedObservationFactory;
        this.redissonClient = redissonClient;
        this.meterRegistry = meterRegistry;
    }


    /**
     * 应用启动完成后初始化所有定时任务。
     *
     * @param event 启动事件（未使用）
     */
    @EventListener(ApplicationStartedEvent.class)
    public void init() {

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
                        throw new BeanCreationException("RedissonScheduled name duplicated, name: " + name);
                    }
                    map.put(name, wrapper(name, annotation, method, bean));
                }
            }
            if (bean instanceof RedissonScheduledService scheduledService) {
                if (map.containsKey(scheduledService.name())) {
                    throw new BeanCreationException("RedissonScheduled name duplicated, name: " + scheduledService.name());
                }
                map.put(scheduledService.name(), wrapper(scheduledService));
            }
        });
    }

    /**
     * 为 {@link RedissonScheduledService} 实例创建执行器。
     *
     * @param service 定时任务服务
     * @return 执行器包装
     */
    private ExecutorWrapper wrapper (RedissonScheduledService service) {
        return new ExecutorWrapper(redissonClient, unifiedObservationFactory,
                service, service.name(), service.initialDelay(),
                service.fixedDelay(), service.stopOnceShutdown(), meterRegistry);
    }
    /**
     * 为 {@link io.github.opensabe.common.redisson.annotation.RedissonScheduled} 方法创建执行器。
     *
     * @param name 任务名称
     * @param annotation 注解实例
     * @param method 目标方法
     * @param bean 目标 Bean
     * @return 执行器包装
     */
    private ExecutorWrapper wrapper (String name, RedissonScheduled annotation, Method method, Object bean) {
        return new ExecutorWrapper(redissonClient, unifiedObservationFactory, () -> method.invoke(bean), name, annotation.initialDelay(),
                annotation.fixedDelay(), annotation.stopOnceShutdown(), meterRegistry);
    }

    /**
     * RefreshScope 刷新后更新任务调度参数与 service 引用。
     *
     * @param service 刷新后的服务实例
     */
    public void refresh (RedissonScheduledService service) {
        ExecutorWrapper wrapper = map.get(service.name());
        if (Objects.isNull(wrapper)) {
            log.warn("RedissonScheduledBeanPostProcessor refresh task: {} failed, can't find ExecutorWrapper.", service.name());
            return;
        }
        wrapper.refresh(service);
    }

    /** 关闭所有定时任务执行器与选主线程。 */
    public void close() {
        log.info("closing RedissonScheduledListener...");
        map.values().parallelStream().forEach(ExecutorWrapper::close);
        log.info("RedissonScheduledListener closed...");
    }

    /** 单任务执行器：选主锁 + 调度线程池 + 耗时指标。 */
    private static class ExecutorWrapper {
        /** 选主锁持有线程。 */
        private final Thread leaderLatch;
        /** 单线程调度器。 */
        private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
        /** 任务耗时分布指标。 */
        private final DistributionSummary distributionSummary;
        /** 任务名称。 */
        private final String name;
        /** 包装观测的任务 Runnable。 */
        private final Runnable task;

        /**
         * 可刷新的任务执行体；RefreshScope 刷新时需更新引用，即使 fixedDelay 未变。
         */
        private volatile ScheduledService service;

        /** 当前初始延迟（毫秒）。 */
        private volatile long initialDelay;
        /** 当前固定间隔（毫秒）。 */
        private volatile long fixedDelay;
        /** 当前调度 Future。 */
        private volatile ScheduledFuture<?> future;

        /** 关闭时是否立即中断。 */
        private volatile boolean stopOnceShutdown;
        /** 当前实例是否为集群 leader。 */
        private volatile boolean isLeader;
        /** 是否已停止。 */
        private volatile boolean isStopped = false;

        /**
         * 创建执行器并启动选主与调度。
         *
         * @param redissonClient Redisson 客户端
         * @param unifiedObservationFactory 观测工厂
         * @param runnable 任务执行体
         * @param name 任务名称
         * @param initialDelay 初始延迟
         * @param fixedDelay 固定间隔
         * @param stopOnceShutdown 关闭策略
         * @param meterRegistry 指标注册表
         */
        ExecutorWrapper(RedissonClient redissonClient, UnifiedObservationFactory unifiedObservationFactory,
                               ScheduledService runnable,
                               String name, long initialDelay,
                               long fixedDelay, boolean stopOnceShutdown, MeterRegistry meterRegistry) {
            this.initialDelay = initialDelay;
            this.fixedDelay = fixedDelay;
            this.name = name;
            this.stopOnceShutdown = stopOnceShutdown;
            this.service = runnable;

            RLock lock = redissonClient.getLock(name + ":leader");
            ThreadFactory build = new ThreadFactoryBuilder().setNameFormat(name + "_scheduler").build();
            this.distributionSummary = DistributionSummary
                    .builder("redisson.schedule.task." + name)
                    .distributionStatisticBufferLength(20)
                    .distributionStatisticExpiry(Duration.ofDays(30))
                    .publishPercentileHistogram(Boolean.TRUE)
                    .publishPercentiles(0.1, 0.5, 0.9)
                    .register(meterRegistry);
            //
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

            this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, build, new ThreadPoolExecutor.AbortPolicy());
            this.task = () -> unifiedObservationFactory.createEmptyObservation().observe(() -> {
                try {
                    if (isLeader) {
                        long start = System.currentTimeMillis();
                        if (log.isDebugEnabled()) {
                            log.debug("RedissonScheduledBeanPostProcessor task: {} start", name);
                        }
                        getService().run();
                        long elapsed = System.currentTimeMillis() - start;
                        if (distributionSummary.count() > 10 && elapsed > distributionSummary.max() * 2 && elapsed > 60000) {
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
            });
            this.future = scheduledThreadPoolExecutor.scheduleAtFixedRate(task , initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
        }


        /**
         * 热更新调度参数；间隔变化时取消旧 Future 并重新 schedule（不中断进行中的任务）。
         *
         * @param service 刷新后的服务
         */
        void refresh (RedissonScheduledService service) {
            if (Objects.equals(this.name, service.name())) {
                if (isStopped || this.scheduledThreadPoolExecutor.isShutdown()) {
                    log.info("RedissonScheduledBeanPostProcessor executor {} is stopped, ignore refresh", name);
                    return;
                }
                // Always refresh service reference even when delays unchanged (other config may have changed)
                setService(service);
                if (this.fixedDelay != service.fixedDelay() || this.initialDelay != service.initialDelay()) {
                    if (this.future != null) {
                        // Do not interrupt in-flight task; new schedule takes effect on next run
                        this.future.cancel(false);
                    }
                    this.future = scheduledThreadPoolExecutor.scheduleAtFixedRate(task, (initialDelay = service.initialDelay()), (fixedDelay = service.fixedDelay()), TimeUnit.MILLISECONDS);
                    log.info("RedissonScheduledBeanPostProcessor executor {} refresh with initialDelay: {}ms, fixedDelay: {}ms", name, initialDelay, fixedDelay);
                }
                this.stopOnceShutdown = service.stopOnceShutdown();
            }
        }

        /** 停止选主线程与调度线程池。 */
        void close() {
            log.info("closing RedissonScheduledBeanPostProcessor executor {} ...", name);
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
            log.info("RedissonScheduledBeanPostProcessor executor {} closed...", name);
        }

        /** @return 当前任务执行体 */
        public ScheduledService getService() {
            return service;
        }

        /** @param service 刷新后的任务执行体 */
        public void setService(ScheduledService service) {
            this.service = service;
        }
    }
}
