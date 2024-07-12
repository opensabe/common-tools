package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.ratelimiter.RRateLimiterSetRateContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RRateLimiterSetRateObservationToJFRGenerator extends ObservationToJFRGenerator<RRateLimiterSetRateContext> {
    @Override
    public Class<RRateLimiterSetRateContext> getContextClazz() {
        return RRateLimiterSetRateContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RRateLimiterSetRateContext context) {
        return context.containsKey(RRateLimiterSetRateJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RRateLimiterSetRateContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RRateLimiterSetRateContext context) {
        RRateLimiterSetRateJFREvent rRateLimiterSetRateJFREvent = context.get(RRateLimiterSetRateJFREvent.class);
        rRateLimiterSetRateJFREvent.setRateSetSuccessfully(context.isSetRateSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rRateLimiterSetRateJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rRateLimiterSetRateJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rRateLimiterSetRateJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RRateLimiterSetRateContext context) {
        context.put(RRateLimiterSetRateJFREvent.class, new RRateLimiterSetRateJFREvent(context));
    }
}
