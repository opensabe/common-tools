package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.ratelimiter.RRateLimiterAcquireContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RRateLimiterAcquireObservationToJFRGenerator extends ObservationToJFRGenerator<RRateLimiterAcquireContext> {
    @Override
    public Class<RRateLimiterAcquireContext> getContextClazz() {
        return RRateLimiterAcquireContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RRateLimiterAcquireContext context) {
        return context.containsKey(RRateLimiterAcquireJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RRateLimiterAcquireContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RRateLimiterAcquireContext context) {
        RRateLimiterAcquireJFREvent rRateLimiterAcquireJFREvent = context.get(RRateLimiterAcquireJFREvent.class);
        rRateLimiterAcquireJFREvent.setAcquireSuccessfully(context.isRateLimiterAcquiredSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rRateLimiterAcquireJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rRateLimiterAcquireJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rRateLimiterAcquireJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RRateLimiterAcquireContext context) {
        RRateLimiterAcquireJFREvent rRateLimiterAcquireJFREvent = new RRateLimiterAcquireJFREvent(context);
        rRateLimiterAcquireJFREvent.begin();
        context.put(RRateLimiterAcquireJFREvent.class, rRateLimiterAcquireJFREvent);
    }
}
