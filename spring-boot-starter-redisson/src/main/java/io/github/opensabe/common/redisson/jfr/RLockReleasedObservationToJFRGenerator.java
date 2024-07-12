package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rlock.RLockReleaseContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RLockReleasedObservationToJFRGenerator extends ObservationToJFRGenerator<RLockReleaseContext> {
    @Override
    public Class<RLockReleaseContext> getContextClazz() {
        return RLockReleaseContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RLockReleaseContext context) {
        return context.containsKey(RLockReleasedJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RLockReleaseContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RLockReleaseContext context) {
        RLockReleasedJFREvent rLockReleasedJFREvent = context.get(RLockReleasedJFREvent.class);
        rLockReleasedJFREvent.setLockReleasedSuccessfully(context.isLockReleasedSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rLockReleasedJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rLockReleasedJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rLockReleasedJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RLockReleaseContext context) {
        context.put(RLockReleasedJFREvent.class, new RLockReleasedJFREvent(context));
    }
}
