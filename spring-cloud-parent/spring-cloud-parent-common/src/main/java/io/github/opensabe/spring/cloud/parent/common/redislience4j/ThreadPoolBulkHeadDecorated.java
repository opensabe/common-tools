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
package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;

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
