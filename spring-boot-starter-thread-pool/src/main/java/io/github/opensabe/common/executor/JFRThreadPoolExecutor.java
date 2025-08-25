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
package io.github.opensabe.common.executor;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Log4j2
public class JFRThreadPoolExecutor implements ExecutorService {

    @Getter
    protected final ThreadPoolExecutor threadPoolExecutor;

    protected final UnifiedObservationFactory unifiedObservationFactory;


    public JFRThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor, UnifiedObservationFactory unifiedObservationFactory) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return threadPoolExecutor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPoolExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return threadPoolExecutor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return threadPoolExecutor.submit(new CustomerCallable<>(unifiedObservationFactory, task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return threadPoolExecutor.submit(new CustomerRunnable(unifiedObservationFactory, task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return threadPoolExecutor.submit(new CustomerRunnable(unifiedObservationFactory, task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<CustomerCallable<T>> collect = tasks.stream()
                .map(call -> new CustomerCallable<>(unifiedObservationFactory, call))
                .collect(Collectors.toList());
        return threadPoolExecutor.invokeAll(collect);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<CustomerCallable<T>> collect = tasks.stream()
                .map(call -> new CustomerCallable<>(unifiedObservationFactory, call))
                .collect(Collectors.toList());
        return threadPoolExecutor.invokeAll(collect, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        List<CustomerCallable<T>> collect = tasks.stream()
                .map(call -> new CustomerCallable<>(unifiedObservationFactory, call))
                .collect(Collectors.toList());
        return threadPoolExecutor.invokeAny(collect);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        List<CustomerCallable<T>> collect = tasks.stream()
                .map(call -> new CustomerCallable<>(unifiedObservationFactory, call))
                .collect(Collectors.toList());
        return threadPoolExecutor.invokeAny(collect, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        threadPoolExecutor.execute(new CustomerRunnable(unifiedObservationFactory, command));
    }
}
