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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.opensabe.common.executor.JFRThreadPoolExecutor;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import io.github.resilience4j.core.ContextPropagator;
import io.github.resilience4j.core.EventConsumer;

/**
 * CustomizedThreadPoolBulkhead。
 */
public class CustomizedThreadPoolBulkhead implements ThreadPoolBulkhead {

/** name。 */
    private final String name;
/** tags。 */
    private final Map<String, String> tags;
/** executor 服务。 */
    private final ExecutorService executorService;
/** config。 */
    private final ThreadPoolBulkheadConfig config;

    public CustomizedThreadPoolBulkhead(String name, Map<String, String> tags, ThreadPoolBulkheadConfig config, ExecutorService executorService) {
        this.name = name;
        this.tags = tags;
        this.executorService = executorService;
        this.config = config;
    }

    /** {@inheritDoc} */
    @Override
    public <T> CompletionStage<T> submit(Callable<T> task) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        try {
            CompletableFuture.supplyAsync(ContextPropagator.decorateSupplier(config.getContextPropagator(), () -> {
                try {
                    return task.call();
                } catch (CompletionException e) {
                    throw e;
                } catch (Throwable e) {
                    throw new CompletionException(e);
                }
            }), executorService).whenComplete((result, throwable) -> {
                if (throwable != null) {
                    promise.completeExceptionally(throwable);
                } else {
                    promise.complete(result);
                }
            });
        } catch (RejectedExecutionException rejected) {
            throw BulkheadFullException.createBulkheadFullException(this);
        }
        return promise;
    }

    /** {@inheritDoc} */
    @Override
    public CompletionStage<Void> submit(Runnable task) {
        final CompletableFuture<Void> promise = new CompletableFuture<>();
        try {
            CompletableFuture.runAsync(ContextPropagator.decorateRunnable(config.getContextPropagator(), () -> {
                try {
                    task.run();
                } catch (Throwable e) {
                    throw new CompletionException(e);
                }
            }), executorService).whenComplete((result, throwable) -> {
                if (throwable != null) {
                    promise.completeExceptionally(throwable);
                } else {
                    promise.complete(result);
                }
            });
        } catch (RejectedExecutionException rejected) {
            throw BulkheadFullException.createBulkheadFullException(this);
        }
        return promise;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public ThreadPoolBulkheadConfig getBulkheadConfig() {
        return config;
    }

    /** {@inheritDoc} */
    @Override
    public Metrics getMetrics() {
        ThreadPoolExecutor threadPoolExecutor = ((JFRThreadPoolExecutor) executorService).getThreadPoolExecutor();
        return new Metrics() {
            /** {@inheritDoc} */
            @Override
            public int getCoreThreadPoolSize() {
                return threadPoolExecutor.getCorePoolSize();
            }

            /** {@inheritDoc} */
            @Override
            public int getThreadPoolSize() {
                return threadPoolExecutor.getPoolSize();
            }

            /** {@inheritDoc} */
            @Override
            public int getMaximumThreadPoolSize() {
                return threadPoolExecutor.getMaximumPoolSize();
            }

            /** {@inheritDoc} */
            @Override
            public int getQueueDepth() {
                return threadPoolExecutor.getQueue().size();
            }

            /** {@inheritDoc} */
            @Override
            public int getRemainingQueueCapacity() {
                return threadPoolExecutor.getQueue().remainingCapacity();
            }

            /** {@inheritDoc} */
            @Override
            public int getQueueCapacity() {
                return config.getQueueCapacity();
            }

            /** {@inheritDoc} */
            @Override
            public int getActiveThreadCount() {
                return threadPoolExecutor.getActiveCount();
            }

            /** {@inheritDoc} */
            @Override
            public int getAvailableThreadCount() {
                return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    /** {@inheritDoc} */
    @Override
    public ThreadPoolBulkheadEventPublisher getEventPublisher() {
        return new ThreadPoolBulkheadEventPublisher() {
            /** {@inheritDoc} */
            @Override
            public ThreadPoolBulkheadEventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer) {
                return this;
            }

            /** {@inheritDoc} */
            @Override
            public ThreadPoolBulkheadEventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer) {
                return this;
            }

            /** {@inheritDoc} */
            @Override
            public ThreadPoolBulkheadEventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer) {
                return this;
            }

            /** {@inheritDoc} */
            @Override
            public void onEvent(EventConsumer<BulkheadEvent> onEventConsumer) {

            }
        };
    }

    /** {@inheritDoc} — 关闭资源。 */
    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            if (!executorService.isTerminated()) {
                executorService.shutdownNow();
            }
            Thread.currentThread().interrupt();
        }
    }
}
