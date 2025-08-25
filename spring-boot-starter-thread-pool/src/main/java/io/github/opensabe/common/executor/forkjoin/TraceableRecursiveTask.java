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

import java.util.concurrent.RecursiveTask;
import io.github.opensabe.common.executor.JFRecordable;
import io.github.opensabe.common.executor.Traceable;
import io.github.opensabe.common.executor.jfr.ThreadTaskJFREvent;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import lombok.Getter;

public abstract class TraceableRecursiveTask<V> extends RecursiveTask<V> implements JFRecordable<V>, Traceable<V> {

    @Getter
    protected final Observation observation;
    private final ThreadTaskJFREvent event;

    public TraceableRecursiveTask(Observation observation) {
        this.observation = observation;
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        this.event = new ThreadTaskJFREvent(
                System.currentTimeMillis(),
                traceContext == null ? null : traceContext.traceId(),
                traceContext == null ? null : traceContext.spanId()
        );
        //对于 JFR 事件，我们先不修改使用消费 Observation 的方式。
        // 原来的封装 Runnable Callable 的方式已经足够好，并且这里的 Observation 的是 scope 的，
        // 不是 start 与 stop，大部分属于子 scope。所以使用原来的方式更好
        this.event.begin();
    }


    @Override
    public ThreadTaskJFREvent getEvent() {
        return event;
    }

    @Override
    protected V compute() {
        return record();
    }

    protected abstract V compute0();

    @Override
    public V inRecord() {
        return trace();
    }

    @Override
    public V inTrace() {
        return compute0();
    }
}
