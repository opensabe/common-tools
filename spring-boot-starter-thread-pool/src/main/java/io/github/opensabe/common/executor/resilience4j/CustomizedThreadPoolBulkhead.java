package io.github.opensabe.common.executor.resilience4j;

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

import java.util.Map;
import java.util.concurrent.*;

public class CustomizedThreadPoolBulkhead implements ThreadPoolBulkhead {

    private final String name;
    private final Map<String, String> tags;
    private final ExecutorService executorService;
    private final ThreadPoolBulkheadConfig config;

    public CustomizedThreadPoolBulkhead(String name, Map<String, String> tags, ThreadPoolBulkheadConfig config, ExecutorService executorService) {
        this.name = name;
        this.tags = tags;
        this.executorService = executorService;
        this.config = config;
    }

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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ThreadPoolBulkheadConfig getBulkheadConfig() {
        return config;
    }

    @Override
    public Metrics getMetrics() {
        ThreadPoolExecutor threadPoolExecutor = ((JFRThreadPoolExecutor) executorService).getThreadPoolExecutor();
        return new Metrics() {
            @Override
            public int getCoreThreadPoolSize() {
                return threadPoolExecutor.getCorePoolSize();
            }

            @Override
            public int getThreadPoolSize() {
                return threadPoolExecutor.getPoolSize();
            }

            @Override
            public int getMaximumThreadPoolSize() {
                return threadPoolExecutor.getMaximumPoolSize();
            }

            @Override
            public int getQueueDepth() {
                return threadPoolExecutor.getQueue().size();
            }

            @Override
            public int getRemainingQueueCapacity() {
                return threadPoolExecutor.getQueue().remainingCapacity();
            }

            @Override
            public int getQueueCapacity() {
                return config.getQueueCapacity();
            }

            @Override
            public int getActiveThreadCount() {
                return threadPoolExecutor.getActiveCount();
            }

            @Override
            public int getAvailableThreadCount() {
                return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
            }
        };
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public ThreadPoolBulkheadEventPublisher getEventPublisher() {
        return new ThreadPoolBulkheadEventPublisher() {
            @Override
            public ThreadPoolBulkheadEventPublisher onCallRejected(EventConsumer<BulkheadOnCallRejectedEvent> eventConsumer) {
                return this;
            }

            @Override
            public ThreadPoolBulkheadEventPublisher onCallPermitted(EventConsumer<BulkheadOnCallPermittedEvent> eventConsumer) {
                return this;
            }

            @Override
            public ThreadPoolBulkheadEventPublisher onCallFinished(EventConsumer<BulkheadOnCallFinishedEvent> eventConsumer) {
                return this;
            }

            @Override
            public void onEvent(EventConsumer<BulkheadEvent> onEventConsumer) {

            }
        };
    }

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
