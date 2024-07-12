package io.github.opensabe.common.dynamodb.jfr;

import io.github.opensabe.common.dynamodb.observation.DynamodbExecuteContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

import java.util.Objects;

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
        }
        event.commit();
    }

    @Override
    protected void generateOnStart(DynamodbExecuteContext context) {
        DynamodbExecuteEvent event = new DynamodbExecuteEvent(context.getMethod(),context.getHashKey(), context.getRangeKey());
        context.put(DynamodbExecuteEvent.class, event);
    }
}
