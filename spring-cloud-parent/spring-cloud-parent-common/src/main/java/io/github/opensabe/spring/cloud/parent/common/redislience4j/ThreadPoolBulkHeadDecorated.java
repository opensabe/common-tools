package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

public class ThreadPoolBulkHeadDecorated implements ThreadPoolBulkhead {
    private final ThreadPoolBulkhead deletgate;

    public ThreadPoolBulkHeadDecorated(ThreadPoolBulkhead deletgate) {
        this.deletgate = deletgate;
    }

    @Override
    public <T> CompletionStage<T> submit(Callable<T> task) {
        return deletgate.submit(task);
    }

    @Override
    public CompletionStage<Void> submit(Runnable task) {
        return deletgate.submit(task);
    }

    @Override
    public String getName() {
        return deletgate.getName();
    }

    @Override
    public ThreadPoolBulkheadConfig getBulkheadConfig() {
        return deletgate.getBulkheadConfig();
    }

    @Override
    public Metrics getMetrics() {
        return deletgate.getMetrics();
    }

    @Override
    public Map<String, String> getTags() {
        return deletgate.getTags();
    }

    @Override
    public ThreadPoolBulkheadEventPublisher getEventPublisher() {
        return deletgate.getEventPublisher();
    }

    @Override
    public void close() throws Exception {
        deletgate.close();
    }
}
