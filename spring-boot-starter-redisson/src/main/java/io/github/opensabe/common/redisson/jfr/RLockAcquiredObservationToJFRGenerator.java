package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rlock.RLockAcquiredContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RLockAcquiredObservationToJFRGenerator extends ObservationToJFRGenerator<RLockAcquiredContext> {
    @Override
    public Class<RLockAcquiredContext> getContextClazz() {
        return RLockAcquiredContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RLockAcquiredContext context) {
        return context.containsKey(RLockAcquiredJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RLockAcquiredContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RLockAcquiredContext context) {
        RLockAcquiredJFREvent rLockAcquiredJFREvent = context.get(RLockAcquiredJFREvent.class);
        rLockAcquiredJFREvent.setLockAcquiredSuccessfully(context.isLockAcquiredSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rLockAcquiredJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rLockAcquiredJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rLockAcquiredJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RLockAcquiredContext context) {
        RLockAcquiredJFREvent rLockAcquiredJFREvent = new RLockAcquiredJFREvent(context);
        rLockAcquiredJFREvent.begin();
        context.put(RLockAcquiredJFREvent.class, rLockAcquiredJFREvent);
    }
}
