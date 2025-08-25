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

import io.github.opensabe.common.executor.jfr.ScheduledThreadTaskJFREvent;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Log4j2
public class JFRScheduledThreadPoolExecutor extends JFRThreadPoolExecutor implements ScheduledExecutorService {

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    public JFRScheduledThreadPoolExecutor(ScheduledThreadPoolExecutor threadPoolExecutor, UnifiedObservationFactory unifiedObservationFactory) {
        super(threadPoolExecutor, unifiedObservationFactory);
        this.scheduledThreadPoolExecutor = threadPoolExecutor;
    }

    protected static class CallableWrapper<T> implements Callable<T> {
        private final Observation observation;
        private final Callable<T> callable;
        private final ScheduledThreadTaskJFREvent threadTaskJFREvent;

        private CallableWrapper(
                UnifiedObservationFactory unifiedObservationFactory, Callable<T> callable,
                long initialDelay, long period, long delay, TimeUnit unit
        ) {
            Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
            this.observation = observation;
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            this.callable = callable;
            this.threadTaskJFREvent = new ScheduledThreadTaskJFREvent(
                    traceContext == null ? null : traceContext.traceId(),
                    traceContext == null ? null : traceContext.spanId(),
                    initialDelay, period, delay, unit
            );
            threadTaskJFREvent.begin();
        }

        @Override
        public T call() throws Exception {
            threadTaskJFREvent.setTaskRunStartTime(System.currentTimeMillis());
            return observation.scoped(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    threadTaskJFREvent.setTaskRunEndTime(System.currentTimeMillis());
                    threadTaskJFREvent.setTaskRunTimeDuration(threadTaskJFREvent.getTaskRunEndTime() - threadTaskJFREvent.getTaskRunStartTime());
                    threadTaskJFREvent.commit();
                }
            });
        }
    }

    protected static class RunnableWrapper implements Runnable {
        private final Observation observation;
        private final Runnable runnable;
        private final ScheduledThreadTaskJFREvent threadTaskJFREvent;

        private RunnableWrapper(UnifiedObservationFactory unifiedObservationFactory,  Runnable runnable,
                                long initialDelay, long period, long delay, TimeUnit unit) {
            Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
            this.observation = observation;
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            this.runnable = runnable;
            this.threadTaskJFREvent = new ScheduledThreadTaskJFREvent(
                    traceContext == null ? null : traceContext.traceId(),
                    traceContext == null ? null : traceContext.spanId(),
                    initialDelay, period, delay, unit
            );
            threadTaskJFREvent.begin();
        }

        @Override
        public void run() {
            threadTaskJFREvent.setTaskRunStartTime(System.currentTimeMillis());
            observation.scoped(() -> {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    log.error("RunnableWrapper-run error: trace: {}-{}, {}", threadTaskJFREvent.getTraceId(), threadTaskJFREvent.getSpanId(), t.getMessage(), t);
                    throw t;
                } finally {
                    threadTaskJFREvent.setTaskRunEndTime(System.currentTimeMillis());
                    threadTaskJFREvent.setTaskRunTimeDuration(threadTaskJFREvent.getTaskRunEndTime() - threadTaskJFREvent.getTaskRunStartTime());
                    threadTaskJFREvent.commit();
                }
            });
        }
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduledThreadPoolExecutor.schedule(
                new RunnableWrapper(unifiedObservationFactory, command, -1, -1, delay, unit),
                delay, unit
        );
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return scheduledThreadPoolExecutor.schedule(
                new CallableWrapper<>(unifiedObservationFactory, callable, -1, -1, delay, unit),
                delay, unit
        );
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledThreadPoolExecutor.scheduleAtFixedRate(
                new RunnableWrapper(unifiedObservationFactory, command, initialDelay, period, -1, unit),
                initialDelay, period, unit
        );
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                new RunnableWrapper(unifiedObservationFactory, command, initialDelay, -1, delay, unit),
                initialDelay, delay, unit
        );
    }
}
