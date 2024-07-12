package io.github.opensabe.common.executor.resilience4j;

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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.*;

@Log4j2
public class ThreadPoolBulkheadCustomizedDecorator implements ThreadPoolBulkHeadDecorator {

    private static final VarHandle executorService;
    private static final VarHandle config;

    static {
        try {
            //初始化句柄
            executorService = MethodHandles.privateLookupIn(FixedThreadPoolBulkhead.class, MethodHandles.lookup())
                    .findVarHandle(FixedThreadPoolBulkhead.class, "executorService", ThreadPoolExecutor.class);
            config = MethodHandles.privateLookupIn(FixedThreadPoolBulkhead.class, MethodHandles.lookup())
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
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService.get(threadPoolBulkhead);
        threadPoolExecutor.shutdownNow();
        ThreadPoolBulkheadConfig threadPoolBulkheadConfig = (ThreadPoolBulkheadConfig) config.get(threadPoolBulkhead);
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
