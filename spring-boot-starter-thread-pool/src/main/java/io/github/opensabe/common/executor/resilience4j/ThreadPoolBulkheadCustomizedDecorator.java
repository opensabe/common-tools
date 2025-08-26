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
package io.github.opensabe.common.executor.resilience4j;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.github.opensabe.common.executor.NamedThreadPoolExecutor;
import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.ThreadUnCaughtExceptionHandler;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.ThreadPoolBulkHeadDecorator;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.internal.FixedThreadPoolBulkhead;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ThreadPoolBulkheadCustomizedDecorator implements ThreadPoolBulkHeadDecorator {

    private static final VarHandle EXECUTOR_SERVICE;
    private static final VarHandle CONFIG;

    static {
        try {
            //初始化句柄
            EXECUTOR_SERVICE = MethodHandles.privateLookupIn(FixedThreadPoolBulkhead.class, MethodHandles.lookup())
                    .findVarHandle(FixedThreadPoolBulkhead.class, "executorService", ThreadPoolExecutor.class);
            CONFIG = MethodHandles.privateLookupIn(FixedThreadPoolBulkhead.class, MethodHandles.lookup())
                    .findVarHandle(FixedThreadPoolBulkhead.class, "config", ThreadPoolBulkheadConfig.class);

        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private final ThreadPoolFactory threadPoolFactory;

    public ThreadPoolBulkheadCustomizedDecorator(ThreadPoolFactory threadPoolFactory) {
        this.threadPoolFactory = threadPoolFactory;
    }

    @SneakyThrows
    @Override
    public ThreadPoolBulkhead decorate(ThreadPoolBulkhead threadPoolBulkhead) {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) EXECUTOR_SERVICE.get(threadPoolBulkhead);
        threadPoolExecutor.shutdownNow();
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = (ThreadPoolBulkheadConfig) CONFIG.get(threadPoolBulkhead);
        String name = "ThreadPoolBulkheadCustomized-" + threadPoolBulkhead.getName();

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(name + "-%d")
                .setUncaughtExceptionHandler(new ThreadUnCaughtExceptionHandler())
                .build();
        ExecutorService customizedThreadPool = threadPoolFactory.createCustomizedThreadPool(
                new NamedThreadPoolExecutor(
                        name,
                        threadPoolBulkheadConfig.getCoreThreadPoolSize(),
                        threadPoolBulkheadConfig.getMaxThreadPoolSize(),
                        threadPoolBulkheadConfig.getKeepAliveDuration().toMillis(),
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<>(threadPoolBulkheadConfig.getQueueCapacity()),
                        threadFactory,
                        threadPoolBulkheadConfig.getRejectedExecutionHandler()
                )
        );
        log.info("ThreadPoolBulkheadCustomizedDecorator-decorate threadPoolBulkhead: {}", name);
        return new CustomizedThreadPoolBulkhead(
                name,
                threadPoolBulkhead.getTags(),
                threadPoolBulkheadConfig,
                customizedThreadPool
        );
    }
}
