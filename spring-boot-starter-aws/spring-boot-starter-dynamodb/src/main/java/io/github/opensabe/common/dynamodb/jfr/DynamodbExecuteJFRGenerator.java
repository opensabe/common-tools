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
package io.github.opensabe.common.dynamodb.jfr;

import java.util.Objects;
import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class DynamodbExecuteJFRGenerator extends ObservationToJFRGenerator<DynamodbExecuteContext> {
    @Override
    public Class<DynamodbExecuteContext> getContextClazz() {
        return DynamodbExecuteContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(DynamodbExecuteContext context) {
        return context.containsKey(DynamodbExecuteEvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(DynamodbExecuteContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(DynamodbExecuteContext context) {
        DynamodbExecuteEvent event = context.get(DynamodbExecuteEvent.class);
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (Objects.nonNull(tracingContext)) {
            TraceContext traceContext = tracingContext.getSpan().context();
            event.setTraceId(traceContext.traceId());
            event.setSpanId(traceContext.spanId());
            event.setHashKey(context.getHashKey());
            event.setRangeKey(context.getRangeKey());
            event.setExpression(context.getExpression());
        }
        event.commit();
    }

    @Override
    protected void generateOnStart(DynamodbExecuteContext context) {
        DynamodbExecuteEvent event = new DynamodbExecuteEvent(context.getMethod(), context.getHashKey(), context.getRangeKey(), context.getExpression());
        context.put(DynamodbExecuteEvent.class, event);
    }
}
