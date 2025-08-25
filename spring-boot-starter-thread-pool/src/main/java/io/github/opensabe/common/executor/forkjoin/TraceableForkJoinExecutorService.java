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
package io.github.opensabe.common.executor.forkjoin;

import io.github.opensabe.common.executor.CustomerCallable;
import io.github.opensabe.common.executor.CustomerRunnable;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 包含 Observation 的 ForkJoinPool
 * 不能使用 delegate 的模式，因为很多核心方法都是 protected 的
 * 这些 protected 方法都和工作窃取或者任务规划相关
 * 通过反射调用可以，但是有两个问题：
 * 1. java.base does not "opens java.lang.invoke" to unnamed module @4157f54e，这样影响我们升级 Java 21
 * 2. 反射性能有问题，虽然可以用 MethodHandle，或者 ASM，但是需要 bind 特定对象，考虑并发问题，还是不太好
 * 所以只能通过继承的方式
 * 同时，如果要提交 ForkJoinTask（因为 exec 方法也是 protected 的），如果不是 TraceableRecursiveTask 的子类，那么可能也会有意想不到的问题
 * 所以，建议提交 TraceableRecursiveTask 的子类，或者 Runnable 或者 Callable
 */
@Log4j2
public class TraceableForkJoinExecutorService extends ForkJoinPool {
    private final UnifiedObservationFactory unifiedObservationFactory;

    public TraceableForkJoinExecutorService(UnifiedObservationFactory unifiedObservationFactory) {
        super();
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    public TraceableForkJoinExecutorService(int parallelism, UnifiedObservationFactory unifiedObservationFactory) {
        super(parallelism);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    public TraceableForkJoinExecutorService(
        int parallelism, ForkJoinWorkerThreadFactory factory,
        Thread.UncaughtExceptionHandler handler, boolean asyncMode,
        UnifiedObservationFactory unifiedObservationFactory
    ) {
        super(parallelism, factory, handler, asyncMode);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    public TraceableForkJoinExecutorService(
        int parallelism, ForkJoinWorkerThreadFactory factory,
        Thread.UncaughtExceptionHandler handler, boolean asyncMode,
        int corePoolSize, int maximumPoolSize, int minimumRunnable,
        Predicate<? super ForkJoinPool> saturate, long keepAliveTime, TimeUnit unit,
        UnifiedObservationFactory unifiedObservationFactory
    ) {
        super(parallelism, factory, handler, asyncMode, corePoolSize, maximumPoolSize, minimumRunnable, saturate, keepAliveTime, unit);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public <T> T invoke(ForkJoinTask<T> task) {
        if (task instanceof TraceableRecursiveTask<T>) {
            return super.invoke(task);
        } else {
            //因为 ForkJoinTask 的 exec 方法是 protected 的
            //虽然可以使用 TraceableRecursiveTask 封装 join 模拟，但是这样还是可能会丢失链路信息
            //不建议使用非 TraceableRecursiveTask 的相关类的 ForkJoinTask
            log.warn("TraceableForkJoinExecutorService-invoke: not TraceableRecursiveTask: {}, observation is not preserved", task.getClass().getName());
            return super.invoke(task);
        }
    }

    @Override
    public void execute(ForkJoinTask<?> task) {
        if (task instanceof TraceableRecursiveTask<?>) {
            super.execute(task);
        } else {
            //因为 ForkJoinTask 的 exec 方法是 protected 的
            //虽然可以使用 TraceableRecursiveTask 封装 join 模拟，但是这样还是可能会丢失链路信息
            //不建议使用非 TraceableRecursiveTask 的相关类的 ForkJoinTask
            log.warn("TraceableForkJoinExecutorService-execute: not TraceableRecursiveTask: {}, observation is not preserved", task.getClass().getName());
            super.execute(task);
        }
    }

    @Override
    public void execute(Runnable task) {
        if (task instanceof CustomerRunnable) {
            super.execute(task);
        } else {
            super.execute(new CustomerRunnable(unifiedObservationFactory, task));
        }
    }

    @Override
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        if (task instanceof TraceableRecursiveTask<T>) {
            return super.submit(task);
        } else {
            //因为 ForkJoinTask 的 exec 方法是 protected 的
            //虽然可以使用 TraceableRecursiveTask 封装 join 模拟，但是这样还是可能会丢失链路信息
            //不建议使用非 TraceableRecursiveTask 的相关类的 ForkJoinTask
            log.warn("TraceableForkJoinExecutorService-submit: not TraceableRecursiveTask: {}, observation is not preserved", task.getClass().getName());
            return super.submit(task);
        }
    }

    @Override
    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        if (task instanceof CustomerCallable) {
            return super.submit(task);
        } else {
            return super.submit(new CustomerCallable<>(unifiedObservationFactory, task));
        }
    }

    @Override
    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        if (task instanceof CustomerRunnable) {
            return super.submit(task, result);
        } else {
            return super.submit(new CustomerRunnable(unifiedObservationFactory, task), result);
        }
    }

    @Override
    public ForkJoinTask<?> submit(Runnable task) {
        if (task instanceof CustomerRunnable) {
            return super.submit(task);
        } else {
            return super.submit(new CustomerRunnable(unifiedObservationFactory, task));
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        List<CustomerCallable<T>> collect = tasks.stream()
            .map(call -> {
                if (call instanceof CustomerCallable) {
                    return (CustomerCallable<T>) call;
                }
                return new CustomerCallable<>(unifiedObservationFactory, call);
            })
            .collect(Collectors.toList());
        return super.invokeAll(collect);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        List<CustomerCallable<T>> collect = tasks.stream()
            .map(call -> {
                if (call instanceof CustomerCallable) {
                    return (CustomerCallable<T>) call;
                }
                return new CustomerCallable<>(unifiedObservationFactory, call);
            })
            .collect(Collectors.toList());
        return super.invokeAll(collect, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        List<CustomerCallable<T>> collect = tasks.stream()
            .map(call -> {
                if (call instanceof CustomerCallable) {
                    return (CustomerCallable<T>) call;
                }
                return new CustomerCallable<>(unifiedObservationFactory, call);
            })
            .collect(Collectors.toList());
        return super.invokeAny(collect);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        List<CustomerCallable<T>> collect = tasks.stream()
            .map(call -> {
                if (call instanceof CustomerCallable) {
                    return (CustomerCallable<T>) call;
                }
                return new CustomerCallable<>(unifiedObservationFactory, call);
            })
            .collect(Collectors.toList());
        return super.invokeAny(collect, timeout, unit);
    }

    @Override
    public ForkJoinWorkerThreadFactory getFactory() {
        return super.getFactory();
    }

    @Override
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return super.getUncaughtExceptionHandler();
    }

    @Override
    public int getParallelism() {
        return super.getParallelism();
    }

    @Override
    public int getPoolSize() {
        return super.getPoolSize();
    }

    @Override
    public boolean getAsyncMode() {
        return super.getAsyncMode();
    }

    @Override
    public int getRunningThreadCount() {
        return super.getRunningThreadCount();
    }

    @Override
    public int getActiveThreadCount() {
        return super.getActiveThreadCount();
    }

    @Override
    public boolean isQuiescent() {
        return super.isQuiescent();
    }

    @Override
    public long getStealCount() {
        return super.getStealCount();
    }

    @Override
    public long getQueuedTaskCount() {
        return super.getQueuedTaskCount();
    }

    @Override
    public int getQueuedSubmissionCount() {
        return super.getQueuedSubmissionCount();
    }

    @Override
    public boolean hasQueuedSubmissions() {
        return super.hasQueuedSubmissions();
    }

    @Override
    protected ForkJoinTask<?> pollSubmission() {
        return super.pollSubmission();
    }

    @Override
    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        return super.drainTasksTo(c);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    @Override
    public boolean isTerminated() {
        return super.isTerminated();
    }

    @Override
    public boolean isTerminating() {
        return super.isTerminating();
    }

    @Override
    public boolean isShutdown() {
        return super.isShutdown();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return super.awaitTermination(timeout, unit);
    }

    @Override
    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        return super.awaitQuiescence(timeout, unit);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (runnable instanceof CustomerRunnable) {
            return super.newTaskFor(runnable, value);
        } else {
            return super.newTaskFor(new CustomerRunnable(unifiedObservationFactory, runnable), value);
        }
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        if (callable instanceof CustomerCallable) {
            return super.newTaskFor(callable);
        } else {
            return super.newTaskFor(new CustomerCallable<>(unifiedObservationFactory, callable));
        }
    }
}
