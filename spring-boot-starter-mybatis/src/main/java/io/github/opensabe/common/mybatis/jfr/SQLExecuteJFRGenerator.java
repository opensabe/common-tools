package io.github.opensabe.common.mybatis.jfr;

import io.github.opensabe.common.mybatis.observation.SQLExecuteContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

import java.util.Objects;

public class SQLExecuteJFRGenerator extends ObservationToJFRGenerator<SQLExecuteContext> {
    @Override
    public Class<SQLExecuteContext> getContextClazz() {
        return SQLExecuteContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(SQLExecuteContext context) {
        return context.containsKey(SQLExecuteEvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(SQLExecuteContext context) {
        return true;
    }

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

    @Override
    protected void generateOnStart(SQLExecuteContext context) {
        SQLExecuteEvent event = new SQLExecuteEvent(context.getMethod(), context.getTransactionName(), context.isSuccess());
        context.put(SQLExecuteEvent.class, event);
        event.begin();
    }
}
