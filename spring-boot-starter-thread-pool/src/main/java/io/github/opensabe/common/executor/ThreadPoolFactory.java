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

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.*;

@Log4j2
@SuppressFBWarnings("EI_EXPOSE_REP")
public class ThreadPoolFactory implements BeanFactoryAware {

    public static boolean isCompleted(ExecutorService executorService) {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
            Thread thread = threadPoolExecutor.getThreadFactory().newThread(() -> log.info("ThreadPoolFactory-isCompleted: {}, activeCount: {}", Thread.currentThread().getName(), threadPoolExecutor.getActiveCount()));
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException ignore) {
            }
            log.info("ThreadPoolFactory-isCompleted: {}, activeCount: {}", threadPoolExecutor, threadPoolExecutor.getActiveCount());
            return threadPoolExecutor.getActiveCount() == 0;
        } else if (executorService instanceof ForkJoinPool) {
            ForkJoinPool forkJoinPool = (ForkJoinPool) executorService;
            log.info("ThreadPoolFactory-isCompleted: {}, activeThreadCount: {}, runningThreadCount: {}, queuedTaskCount: {}, queuedSubmissionCount: {}",
                    forkJoinPool, forkJoinPool.getActiveThreadCount(), forkJoinPool.getRunningThreadCount(), forkJoinPool.getQueuedTaskCount(), forkJoinPool.getQueuedSubmissionCount());
            return forkJoinPool.getActiveThreadCount() == 0
                    && forkJoinPool.getRunningThreadCount() == 0
                    && forkJoinPool.getQueuedTaskCount() == 0
                    && forkJoinPool.getQueuedSubmissionCount() == 0;
        }
        log.error("ThreadPoolFactory-isCompleted: unknown executor service type: {}", executorService);
        return true;
    }

    private final Set<WeakReference<ExecutorService>> allExecutors = Sets.newConcurrentHashSet();

    private UnifiedObservationFactory unifiedObservationFactory;


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.unifiedObservationFactory = beanFactory.getBean(UnifiedObservationFactory.class);
    }

    public Set<WeakReference<ExecutorService>> getAllExecutors() {
        return allExecutors;
    }

    //每个线程池大小不超过1024
    private static final int MAX_THREAD_SIZE_INCLUSIVE = 2 << 10;
    //最大大小
    static final int MAX_CAP = 0x7fff;

    /**
     * 验证大小一定是2的n次方并且小于等于1024
     *
     * @param size
     */
    private static void validThreadPoolSize(int size) {
        assert size <= MAX_THREAD_SIZE_INCLUSIVE && ((size & (size - 1)) == 0);
    }

    /**
     * 定时任务线程池
     *
     * @param threadNameFormat
     * @param coreSize
     * @return
     */
    public ScheduledExecutorService createScheduledThreadPoolExecutor(String threadNameFormat, int coreSize) {
        ThreadFactory build = getThreadFactory(threadNameFormat);
        return new JFRScheduledThreadPoolExecutor(
                new ScheduledThreadPoolExecutor(
                        coreSize,
                        build,
                        new ThreadPoolExecutor.AbortPolicy()
                ), unifiedObservationFactory);
    }

    private static ThreadFactory getThreadFactory(String threadNamePrefix) {
        if (!threadNamePrefix.contains("%d")) {
            threadNamePrefix = threadNamePrefix + "-%d";
        }
        return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix)
                .setUncaughtExceptionHandler(new ThreadUnCaughtExceptionHandler())
                .build();
    }

    /**
     * 创建任务绝对有序的单线程线程池
     *
     * @param threadNamePrefix
     * @return
     */
    public ExecutorService createSingleThreadPoolExecutor(String threadNamePrefix) {
        ThreadFactory build = getThreadFactory(threadNamePrefix);
        ThreadPoolExecutor threadPoolExecutor = new NamedThreadPoolExecutor(
                threadNamePrefix,
                1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(MAX_CAP),
                build,
                new ThreadPoolExecutor.AbortPolicy()
        );
        threadPoolExecutor.prestartAllCoreThreads();
        allExecutors.add(new WeakReference<>(threadPoolExecutor));
        return new JFRThreadPoolExecutor(threadPoolExecutor, unifiedObservationFactory);
    }

    /**
     * 创建普通任务线程池
     *
     * @param threadNamePrefix
     * @param size
     * @return
     */
    public ExecutorService createNormalThreadPool(String threadNamePrefix, int size) {
        ThreadFactory build = getThreadFactory(threadNamePrefix);
        ThreadPoolExecutor threadPoolExecutor = new NamedThreadPoolExecutor(
                threadNamePrefix,
                size, size,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(50240),
                build,
                new ThreadPoolExecutor.AbortPolicy()
        );
        allExecutors.add(new WeakReference<>(threadPoolExecutor));
        threadPoolExecutor.prestartAllCoreThreads();
        return new JFRThreadPoolExecutor(threadPoolExecutor, unifiedObservationFactory);
    }

    public void addWeakReference (ExecutorService executorService) {
        this.allExecutors.add(new WeakReference<>(executorService));
    }

    /**
     * 创建自定义线程池
     *
     * @param threadPoolExecutor
     * @return
     */
    public ExecutorService createCustomizedThreadPool(NamedThreadPoolExecutor threadPoolExecutor) {
        allExecutors.add(new WeakReference<>(threadPoolExecutor));
        threadPoolExecutor.prestartAllCoreThreads();
        return new JFRThreadPoolExecutor(threadPoolExecutor, unifiedObservationFactory);
    }
}
