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
package io.github.opensabe.common.mybatis.jfr;

import java.util.Objects;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.common.mybatis.observation.SQLExecuteContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

/**
 * Observation 到 JFR 的桥接生成器（SQLExecuteJFRGenerator）。
 */
public class SQLExecuteJFRGenerator extends ObservationToJFRGenerator<SQLExecuteContext> {
    /** {@inheritDoc} */
    @Override
    public Class<SQLExecuteContext> getContextClazz() {
        return SQLExecuteContext.class;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean shouldCommitOnStop(SQLExecuteContext context) {
        return context.containsKey(SQLExecuteEvent.class);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean shouldGenerateOnStart(SQLExecuteContext context) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void commitOnStop(SQLExecuteContext context) {
        SQLExecuteEvent event = context.get(SQLExecuteEvent.class);
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (Objects.nonNull(tracingContext)) {
            TraceContext traceContext = tracingContext.getSpan().context();
            event.setTraceId(traceContext.traceId());
            event.setSpanId(traceContext.spanId());
        }
        event.commit();
    }

    /** {@inheritDoc} */
    @Override
    protected void generateOnStart(SQLExecuteContext context) {
        SQLExecuteEvent event = new SQLExecuteEvent(context.getMethod(), context.getTransactionName(), context.isSuccess());
        context.put(SQLExecuteEvent.class, event);
        event.begin();
    }
}
