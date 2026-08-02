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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.github.opensabe.common.executor.CustomerCallable;
import io.github.opensabe.common.executor.CustomerRunnable;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.extern.log4j.Log4j2;

/**
 * 集成 Micrometer Observation 的 {@link ForkJoinPool} 子类。
 * <p>
 * 无法采用委托模式，因大量核心方法为 {@code protected} 且与工作窃取、任务调度相关。
 * 反射调用虽可行，但存在 Java 模块开放限制与性能问题，故通过继承实现。
 * <p>
 * 提交 {@link ForkJoinTask} 时，若非 {@link TraceableRecursiveTask} 子类，
 * 可能无法保留完整链路信息；建议提交 {@link TraceableRecursiveTask}、{@link Runnable} 或 {@link Callable}。
 */
@Log4j2
public class TraceableForkJoinExecutorService extends ForkJoinPool {

    /**
     * 统一 Observation 工厂，用于包装 Runnable/Callable。
     */
    private final UnifiedObservationFactory unifiedObservationFactory;

    /**
     * 使用默认并行度创建线程池。
     *
     * @param unifiedObservationFactory Observation 工厂
     */
    public TraceableForkJoinExecutorService(UnifiedObservationFactory unifiedObservationFactory) {
        super();
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /**
     * 指定并行度创建线程池。
     *
     * @param parallelism               并行度
     * @param unifiedObservationFactory Observation 工厂
     */
    public TraceableForkJoinExecutorService(int parallelism, UnifiedObservationFactory unifiedObservationFactory) {
        super(parallelism);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /**
     * 指定并行度、线程工厂、异常处理器与异步模式创建线程池。
     *
     * @param parallelism               并行度
     * @param factory                   工作线程工厂
     * @param handler                   未捕获异常处理器
     * @param asyncMode                 是否异步模式
     * @param unifiedObservationFactory Observation 工厂
     */
    public TraceableForkJoinExecutorService(
            int parallelism, ForkJoinWorkerThreadFactory factory,
            Thread.UncaughtExceptionHandler handler, boolean asyncMode,
            UnifiedObservationFactory unifiedObservationFactory
    ) {
        super(parallelism, factory, handler, asyncMode);
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    /**
     * 使用完整 {@link ForkJoinPool} 构造参数创建线程池。
     *
     * @param parallelism               并行度
     * @param factory                   工作线程工厂
     * @param handler                   未捕获异常处理器
     * @param asyncMode                 是否异步模式
     * @param corePoolSize              核心池大小
     * @param maximumPoolSize           最大池大小
     * @param minimumRunnable           最小可运行线程数
     * @param saturate                  饱和策略谓词
     * @param keepAliveTime             空闲线程存活时间
     * @param unit                      存活时间单位
     * @param unifiedObservationFactory Observation 工厂
     */
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

    /**
     * 执行 {@link ForkJoinTask} 并等待结果；非 {@link TraceableRecursiveTask} 时记录警告。
     *
     * @param task 待执行任务
     * @param <T>  结果类型
     * @return 任务结果
     */
    @Override
    public <T> T invoke(ForkJoinTask<T> task) {
        if (task instanceof TraceableRecursiveTask<T>) {
            return super.invoke(task);
        } else {
            log.warn("TraceableForkJoinExecutorService-invoke: not TraceableRecursiveTask: {}, observation is not preserved", task.getClass().getName());
            return super.invoke(task);
        }
    }

    /**
     * 提交 {@link ForkJoinTask} 异步执行；非 {@link TraceableRecursiveTask} 时记录警告。
     *
     * @param task 待执行任务
     */
    @Override
    public void execute(ForkJoinTask<?> task) {
        if (task instanceof TraceableRecursiveTask<?>) {
            super.execute(task);
        } else {
            log.warn("TraceableForkJoinExecutorService-execute: not TraceableRecursiveTask: {}, observation is not preserved", task.getClass().getName());
            super.execute(task);
        }
    }

    /**
     * 提交 {@link Runnable}；非 {@link CustomerRunnable} 时自动包装以传播 Observation。
     *
     * @param task 待执行任务
     */
    @Override
    public void execute(Runnable task) {
        if (task instanceof CustomerRunnable) {
            super.execute(task);
        } else {
            super.execute(new CustomerRunnable(unifiedObservationFactory, task));
        }
    }

    /**
     * 提交 {@link ForkJoinTask} 并返回 Future；非 {@link TraceableRecursiveTask} 时记录警告。
     *
     * @param task 待提交任务
     * @param <T>  结果类型
     * @return 任务 Future
     */
    @Override
    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        if (task instanceof TraceableRecursiveTask<T>) {
            return super.submit(task);
        } else {
            log.warn("TraceableForkJoinExecutorService-submit: not TraceableRecursiveTask: {}, observation is not preserved", task.getClass().getName());
            return super.submit(task);
        }
    }

    /**
     * 提交 {@link Callable}；非 {@link CustomerCallable} 时自动包装。
     *
     * @param task 待提交任务
     * @param <T>  结果类型
     * @return 任务 Future
     */
    @Override
    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        if (task instanceof CustomerCallable) {
            return super.submit(task);
        } else {
            return super.submit(new CustomerCallable<>(unifiedObservationFactory, task));
        }
    }

    /**
     * 提交带结果的 {@link Runnable}；非 {@link CustomerRunnable} 时自动包装。
     *
     * @param task   待提交任务
     * @param result 预设结果
     * @param <T>    结果类型
     * @return 任务 Future
     */
    @Override
    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        if (task instanceof CustomerRunnable) {
            return super.submit(task, result);
        } else {
            return super.submit(new CustomerRunnable(unifiedObservationFactory, task), result);
        }
    }

    /**
     * 提交 {@link Runnable} 并返回 Future；非 {@link CustomerRunnable} 时自动包装。
     *
     * @param task 待提交任务
     * @return 任务 Future
     */
    @Override
    public ForkJoinTask<?> submit(Runnable task) {
        if (task instanceof CustomerRunnable) {
            return super.submit(task);
        } else {
            return super.submit(new CustomerRunnable(unifiedObservationFactory, task));
        }
    }

    /**
     * 批量提交 {@link Callable} 并等待全部完成；自动包装为 {@link CustomerCallable}。
     *
     * @param tasks 任务集合
     * @param <T>   结果类型
     * @return Future 列表
     * @throws InterruptedException 等待被中断
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
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

    /**
     * 在超时时间内批量提交 {@link Callable} 并等待；自动包装为 {@link CustomerCallable}。
     *
     * @param tasks   任务集合
     * @param timeout 超时时间
     * @param unit    时间单位
     * @param <T>     结果类型
     * @return Future 列表
     * @throws InterruptedException 等待被中断
     */
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

    /**
     * 批量提交 {@link Callable} 并返回任一成功结果；自动包装为 {@link CustomerCallable}。
     *
     * @param tasks 任务集合
     * @param <T>   结果类型
     * @return 首个成功结果
     * @throws InterruptedException 等待被中断
     * @throws ExecutionException   任务执行异常
     */
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

    /**
     * 在超时时间内批量提交 {@link Callable} 并返回任一成功结果。
     *
     * @param tasks   任务集合
     * @param timeout 超时时间
     * @param unit    时间单位
     * @param <T>     结果类型
     * @return 首个成功结果
     * @throws InterruptedException 等待被中断
     * @throws ExecutionException   任务执行异常
     * @throws TimeoutException     超时
     */
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

    /** {@inheritDoc} */
    @Override
    public ForkJoinWorkerThreadFactory getFactory() {
        return super.getFactory();
    }

    /** {@inheritDoc} */
    @Override
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return super.getUncaughtExceptionHandler();
    }

    /** {@inheritDoc} */
    @Override
    public int getParallelism() {
        return super.getParallelism();
    }

    /** {@inheritDoc} */
    @Override
    public int getPoolSize() {
        return super.getPoolSize();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getAsyncMode() {
        return super.getAsyncMode();
    }

    /** {@inheritDoc} */
    @Override
    public int getRunningThreadCount() {
        return super.getRunningThreadCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getActiveThreadCount() {
        return super.getActiveThreadCount();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isQuiescent() {
        return super.isQuiescent();
    }

    /** {@inheritDoc} */
    @Override
    public long getStealCount() {
        return super.getStealCount();
    }

    /** {@inheritDoc} */
    @Override
    public long getQueuedTaskCount() {
        return super.getQueuedTaskCount();
    }

    /** {@inheritDoc} */
    @Override
    public int getQueuedSubmissionCount() {
        return super.getQueuedSubmissionCount();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasQueuedSubmissions() {
        return super.hasQueuedSubmissions();
    }

    /** {@inheritDoc} */
    @Override
    protected ForkJoinTask<?> pollSubmission() {
        return super.pollSubmission();
    }

    /** {@inheritDoc} */
    @Override
    protected int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        return super.drainTasksTo(c);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        super.shutdown();
    }

    /** {@inheritDoc} */
    @Override
    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTerminated() {
        return super.isTerminated();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isTerminating() {
        return super.isTerminating();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isShutdown() {
        return super.isShutdown();
    }

    /** {@inheritDoc} */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return super.awaitTermination(timeout, unit);
    }

    /** {@inheritDoc} */
    @Override
    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        return super.awaitQuiescence(timeout, unit);
    }

    /**
     * 为带结果的 {@link Runnable} 创建 {@link RunnableFuture}；非 {@link CustomerRunnable} 时自动包装。
     *
     * @param runnable 任务
     * @param value    预设结果
     * @param <T>      结果类型
     * @return RunnableFuture
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (runnable instanceof CustomerRunnable) {
            return super.newTaskFor(runnable, value);
        } else {
            return super.newTaskFor(new CustomerRunnable(unifiedObservationFactory, runnable), value);
        }
    }

    /**
     * 为 {@link Callable} 创建 {@link RunnableFuture}；非 {@link CustomerCallable} 时自动包装。
     *
     * @param callable 任务
     * @param <T>      结果类型
     * @return RunnableFuture
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        if (callable instanceof CustomerCallable) {
            return super.newTaskFor(callable);
        } else {
            return super.newTaskFor(new CustomerCallable<>(unifiedObservationFactory, callable));
        }
    }
}
