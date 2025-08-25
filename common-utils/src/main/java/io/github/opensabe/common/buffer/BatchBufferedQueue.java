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
package io.github.opensabe.common.buffer;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jctools.queues.MpscBlockingConsumerArrayQueue;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.ForOverride;

import io.github.opensabe.common.buffer.observation.BatchBufferQueueBatchManipulateObservationDocumentation;
import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.ThreadPoolFactoryGracefulShutDownHandler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

/**
 * 内存批次队列，主要是将小事务合并
 * 合并的原因参考：
 * 1. https://aws.amazon.com/cn/blogs/database/analyze-amazon-aurora-mysql-workloads-with-performance-insights/
 * 2. https://www.modb.pro/db/70174
 * 主要是针对同时满足如下这些条件的场景进行优化：
 * 1. 有很多并发用户更新某个表的不同行，同时这些更新，并没有被事务包裹，而是 auto commit 的，也就是每个语句是一个小事务
 * 2. 这个更新，不必立刻读取出来，而是允许延迟，允许最终一致
 * 3. 这个更新，允许丢失，业务自己做补偿与健壮性保证
 *
 * @param <E> 实现了 BufferedElement 接口的方法
 */
@Log4j2
public abstract class BatchBufferedQueue<E extends BufferedElement> {
    protected static final int DEFAULT_QUEUE_COUNT = 1;
    protected static final int DEFAULT_QUEUE_SIZE = 1048576;
    protected static final int DEFAULT_BATCH_SIZE = 2048;
    protected static final long DEFAULT_POLL_WAIT_TIME_IN_MILLIS = 1000L;
    protected static final long DEFAULT_MAX_WAIT_TIME_IN_MILLIS = 1000L;
    /**
     * 负载均衡使用，使用这个决定 hashKey 返回空的提交到哪个队列
     */
    private final AtomicInteger counter = new AtomicInteger(0);
    /**
     * 每个队列都是单线程消费者，但是是多线程提交，所以使用 MPSC
     */
    private MpscBlockingConsumerArrayQueue<E>[] mpscBlockingConsumerArrayQueues;
    /**
     * 每个队列一个单线程池
     */
    private ExecutorService[] executorServices;
    /**
     * 公共线程池工厂
     */
    @Autowired
    private ThreadPoolFactory threadPoolFactory;

    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Autowired
    private ThreadPoolFactoryGracefulShutDownHandler threadPoolFactoryGracefulShutDownHandler;

    /**
     * 找大于这个 n 的最近的 2^n
     *
     * @param n
     * @return
     */
    private static int getNearest2Power(int n) {
        //如果已经是 2^n 就直接返回
        if ((n & (n - 1)) == 0) {
            return n;
        }
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        n += 1;
        return n;
    }

    /**
     * @return 队列个数
     */
    @ForOverride
    protected int queueCount() {
        return DEFAULT_QUEUE_COUNT;
    }

    /**
     * @return 队列大小
     */
    @ForOverride
    protected int queueSize() {
        return DEFAULT_QUEUE_SIZE;
    }

    /**
     * @return 批次大小，达到批次大小开始调用 batchManipulate
     */
    @ForOverride
    protected int batchSize() {
        return DEFAULT_BATCH_SIZE;
    }

    /**
     * @return 循环中每次 poll 等待时间，调高这个可以减少 CPU 消耗，但是会增大消息批量处理的延迟
     */
    @ForOverride
    protected long pollWaitTimeInMillis() {
        return DEFAULT_POLL_WAIT_TIME_IN_MILLIS;
    }

    /**
     * @return 本次 batch 等待 poll 消息的时间总和界限，超过则直接开始调用 batchManipulate
     */
    @ForOverride
    protected long maxWaitTimeInMillis() {
        return DEFAULT_MAX_WAIT_TIME_IN_MILLIS;
    }

    /**
     * 比较器，最后回调的 batchManipulate 中的列表是有序的，顺序按照这里比较器排序
     *
     * @return E 的比较器
     */
    protected abstract Comparator<E> comparator();

    /**
     * 需要实现这个方法，批量处理提交到队列中的对象
     *
     * @param batch
     */
    protected abstract void batchManipulate(List<E> batch);

    protected void beforeExecute(List<E> batch) {

    }

    protected void afterBatchFinish(List<E> batch) {

    }

    protected void afterBatchError(List<E> batch, Throwable throwable) {

    }

    private void manipulate(List<E> batch) {
        Observation observation = BatchBufferQueueBatchManipulateObservationDocumentation.DEFAULT_OBSERVATION_DOCUMENTATION.observation(unifiedObservationFactory.getObservationRegistry());
        if (CollectionUtils.isEmpty(batch)) {
            log.debug("BatchBufferedQueue: empty queue");
            return;
        }
        log.info("BatchBufferedQueue: current batch size: {}", batch.size());
        try {
            observation.start();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            batch.forEach(e -> {
                e.beforeElementManipulate(traceContext.spanId());
            });
            batch = batch.stream().sorted(comparator()).collect(Collectors.toList());
            beforeExecute(batch);
            batchManipulate(batch);
            batch.forEach(BufferedElement::afterElementManipulate);
            afterBatchFinish(batch);
        } catch (Throwable e) {
            batch.forEach(element -> {
                element.afterElementManipulateError(e);
            });
            afterBatchError(batch, e);
            observation.error(e);
        } finally {
            observation.stop();
        }
    }

    /**
     * 初始化方法， Spring 框架会调用
     */
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void init() {
        //获取各个参数大小，以下队列大小以及批次相关的需要是 2^n 来提高效率
        final int queueCount = getNearest2Power(queueCount());
        final int queueSize = getNearest2Power(queueSize());
        final int batchSize = getNearest2Power(batchSize());
        //获取超时时间配置
        final long pollWaitTimeInMillis = pollWaitTimeInMillis();
        final long maxWaitTimeInMillis = maxWaitTimeInMillis();
        //final Comparator<E> comparator = comparator();
        final String name = getClass().getSimpleName();

        mpscBlockingConsumerArrayQueues = new MpscBlockingConsumerArrayQueue[queueCount];
        executorServices = new ExecutorService[queueCount];

        for (int i = 0; i < queueCount; i++) {
            mpscBlockingConsumerArrayQueues[i] = new MpscBlockingConsumerArrayQueue<>(queueSize);
            executorServices[i] = threadPoolFactory.createSingleThreadPoolExecutor(name + "-" + i);
            int finalI = i;
            executorServices[i].submit(() -> {
                Thread thread = Thread.currentThread();
                while (!thread.isInterrupted() &&
                        //如果在优雅关闭，则结束
                        !threadPoolFactoryGracefulShutDownHandler.isShuttingDown()) {
                    try {
                        List<E> batch = Lists.newArrayList();
                        long start = System.currentTimeMillis();
                        while (batch.size() < batchSize) {
                            E queueElement = mpscBlockingConsumerArrayQueues[finalI].poll(pollWaitTimeInMillis, TimeUnit.MILLISECONDS);
                            if (queueElement != null) {
                                //有新任务
                                batch.add(queueElement);
                                log.info("BatchBufferedQueue: {} origin traceId: {} spanId: {} add to batch", name, queueElement.traceId(), queueElement.spanId());
                            } else {
                                //一秒都没有拉取新任务直接退出，执行 batch
                                break;
                            }
                            //极限情况下如果消息一秒一秒到的，那么最多会等 finalBatchSize * 1s，我们限制最多等 maxWaitTimeInMills
                            if (System.currentTimeMillis() - start > maxWaitTimeInMillis) {
                                break;
                            }
                        }
                        manipulate(batch);
                    } catch (Throwable e) {
                        log.fatal("BatchBufferedQueue: {} error: {}", name, e.getMessage(), e);
                    }
                }
                log.info("BatchBufferedQueue: {} before shutting down, drain all remaining: {}", name, mpscBlockingConsumerArrayQueues[finalI].size());
                List<E> batch = Lists.newArrayList();
                E queueElement;
                while ((queueElement = mpscBlockingConsumerArrayQueues[finalI].relaxedPoll()) != null) {
                    batch.add(queueElement);
                    log.info("BatchBufferedQueue: {} origin traceId: {} spanId: {} add to batch", name, queueElement.traceId(), queueElement.spanId());
                    if (batch.size() >= batchSize) {
                        manipulate(batch);
                        batch.clear();
                    }
                }
                if (CollectionUtils.isNotEmpty(batch)) {
                    manipulate(batch);
                }
                log.info("BatchBufferedQueue {} shutdown", name);
            });
        }
    }

    /**
     * 提交任务对象到队列中
     *
     * @param e
     */
    public void submit(E e) {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        e.setSubmitInfo(
                traceContext == null ? null : traceContext.traceId(),
                traceContext == null ? null : traceContext.spanId()
        );
        log.info("BatchBufferedQueue-sumbit: {} -> {}", getClass().getSimpleName(), e.hashKey());
        String hashKey = e.hashKey();
        int length = mpscBlockingConsumerArrayQueues.length;
        int idx;
        if (StringUtils.isNotBlank(hashKey)) {
            //对 2^n 取余数 = 对 2^n - 1 取与运算
            idx = hashKey.hashCode() & (length - 1);
        } else {
            //对 2^n 取余数 = 对 2^n - 1 取与运算
            idx = Math.abs(counter.incrementAndGet() & (length - 1));
        }
        mpscBlockingConsumerArrayQueues[idx].offer(e);
    }

}
