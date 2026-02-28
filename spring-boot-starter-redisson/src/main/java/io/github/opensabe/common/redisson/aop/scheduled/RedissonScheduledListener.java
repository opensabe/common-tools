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
import java.util.function.Supplier;


@Log4j2
public class RedissonScheduledListener {

    private final RedissonScheduledBeanPostProcessor processor;
    private final UnifiedObservationFactory unifiedObservationFactory;
    private final RedissonClient redissonClient;
    private final MeterRegistry meterRegistry;

    private final Map<String, ExecutorWrapper> map = Maps.newConcurrentMap();


    public RedissonScheduledListener(RedissonScheduledBeanPostProcessor processor, UnifiedObservationFactory unifiedObservationFactory, RedissonClient redissonClient, MeterRegistry meterRegistry) {
        this.processor = processor;
        this.unifiedObservationFactory = unifiedObservationFactory;
        this.redissonClient = redissonClient;
        this.meterRegistry = meterRegistry;
    }


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

    private ExecutorWrapper wrapper (RedissonScheduledService service) {
        return new ExecutorWrapper(redissonClient, unifiedObservationFactory,
                service, service.name(), service.initialDelay(),
                service.fixedDelay(), service.stopOnceShutdown(), meterRegistry);
    }
    private ExecutorWrapper wrapper (String name, RedissonScheduled annotation, Method method, Object bean) {
        return new ExecutorWrapper(redissonClient, unifiedObservationFactory, () -> method.invoke(bean), name, annotation.initialDelay(),
                annotation.fixedDelay(), annotation.stopOnceShutdown(), meterRegistry);
    }

    public void refresh (RedissonScheduledService service) {
        ExecutorWrapper wrapper = map.get(service.name());
        if (Objects.isNull(wrapper)) {
            log.warn("RedissonScheduledBeanPostProcessor refresh task: {} failed, can't find ExecutorWrapper.", service.name());
        }
        wrapper.refresh(service);
    }

    public void close() {
        log.info("closing RedissonScheduledListener...");
        map.values().parallelStream().forEach(ExecutorWrapper::close);
        log.info("RedissonScheduledListener closed...");
    }

    private static class ExecutorWrapper {
        private final Thread leaderLatch;
        private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
        private final DistributionSummary distributionSummary;
        private final String name;
        private final Supplier<Runnable> task;

        /**
         * 如果service是RefreshScope，其他属性刷新，但是fixedDelay属性没刷新，此时线程池保存的还是旧的bean
         * 因此需要把service做成本地变量，bean刷新时，即使不重新启动定时任务，也要更新一下service
         */
        private volatile ScheduledService service;

        private volatile long initialDelay;
        private volatile long fixedDelay;
        private volatile ScheduledFuture<?> future;

        private volatile boolean stopOnceShutdown;
        private volatile boolean isLeader;
        private volatile boolean isStopped = false;

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
            this.task = () -> () -> unifiedObservationFactory.createEmptyObservation().observe(() -> {
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
            this.future = scheduledThreadPoolExecutor.scheduleAtFixedRate(task.get() , initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
        }


        void refresh (RedissonScheduledService service) {
            if (Objects.equals(this.name, service.name())) {
                if (isStopped || this.scheduledThreadPoolExecutor.isShutdown()) {
                    log.info("RedissonScheduledBeanPostProcessor executor {} is stopped, ignore refresh", name);
                    return;
                }
                //即使fixedDelay和initialDelay没有修改，也要更新service,否则其他关键配置还是旧的
                setService(service);
                if (this.fixedDelay != service.fixedDelay() || this.initialDelay != service.initialDelay()) {
                    if (this.future != null) {
                        //不强制中断进行中的任务，等执行完当前的任务，下次生效
                        this.future.cancel(false);
                    }
                    this.future = scheduledThreadPoolExecutor.scheduleAtFixedRate(task.get(), (initialDelay = service.initialDelay()), (fixedDelay = service.fixedDelay()), TimeUnit.MILLISECONDS);
                    log.info("RedissonScheduledBeanPostProcessor executor {} refresh with initialDelay: {}ms, fixedDelay: {}ms", name, initialDelay, fixedDelay);
                }
                this.stopOnceShutdown = service.stopOnceShutdown();
            }
        }

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

        public ScheduledService getService() {
            return service;
        }

        public void setService(ScheduledService service) {
            this.service = service;
        }
    }
}
