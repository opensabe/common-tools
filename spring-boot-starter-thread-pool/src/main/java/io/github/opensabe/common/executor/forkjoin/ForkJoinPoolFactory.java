package io.github.opensabe.common.executor.forkjoin;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.ThreadUnCaughtExceptionHandler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ForkJoinPoolFactory {
    private final ThreadPoolFactory threadPoolFactory;

    private final UnifiedObservationFactory unifiedObservationFactory;

    public ForkJoinPoolFactory(ThreadPoolFactory threadPoolFactory, UnifiedObservationFactory unifiedObservationFactory) {
        this.threadPoolFactory = threadPoolFactory;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    public TraceableForkJoinExecutorService createForkJoinTaskThreadPool(String threadNamePrefix, int size) {
        NamedForkJoinWorkerThreadFactory namedForkJoinWorkerThreadFactory = new NamedForkJoinWorkerThreadFactory(threadNamePrefix);
        TraceableForkJoinExecutorService traceableForkJoinExecutorService = new TraceableForkJoinExecutorService(size,
                namedForkJoinWorkerThreadFactory,
                new ThreadUnCaughtExceptionHandler(),
                false,
                0,
                32767,
                1, null,
                60L, TimeUnit.SECONDS, unifiedObservationFactory);
        threadPoolFactory.addWeakReference(traceableForkJoinExecutorService);
        return traceableForkJoinExecutorService;
    }

    public class NamedForkJoinWorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        final String name;
        final AtomicLong COUNTER = new AtomicLong();

        public NamedForkJoinWorkerThreadFactory(String name) {
            this.name = name;
        }



        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            var thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setName(name + "-" +COUNTER.getAndIncrement());
            return thread;
        }
    }

}
