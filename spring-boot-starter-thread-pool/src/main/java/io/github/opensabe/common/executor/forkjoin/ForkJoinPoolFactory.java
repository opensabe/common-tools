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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.ThreadUnCaughtExceptionHandler;
import io.github.opensabe.common.observation.UnifiedObservationFactory;

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
            thread.setName(name + "-" + COUNTER.getAndIncrement());
            return thread;
        }
    }

}
