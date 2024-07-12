package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rexpirable.RExpirableExpireContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RExpirableExpireObservationToJFRGenerator extends ObservationToJFRGenerator<RExpirableExpireContext> {
    @Override
    public Class<RExpirableExpireContext> getContextClazz() {
        return RExpirableExpireContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RExpirableExpireContext context) {
        return context.containsKey(RExpirableExpireJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RExpirableExpireContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RExpirableExpireContext context) {
        RExpirableExpireJFREvent rExpirableExpireJFREvent = context.get(RExpirableExpireJFREvent.class);
        rExpirableExpireJFREvent.setExpireSetSuccessfully(context.isExpireSetSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            rExpirableExpireJFREvent.setTraceId(traceContext.traceId());
            rExpirableExpireJFREvent.setSpanId(traceContext.spanId());
        }
        rExpirableExpireJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RExpirableExpireContext context) {
        context.put(RExpirableExpireJFREvent.class, new RExpirableExpireJFREvent(context));
    }
}