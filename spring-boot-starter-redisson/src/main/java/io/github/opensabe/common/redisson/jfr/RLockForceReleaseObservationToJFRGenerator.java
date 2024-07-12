package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rlock.RLockForceReleaseContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RLockForceReleaseObservationToJFRGenerator extends ObservationToJFRGenerator<RLockForceReleaseContext> {
    @Override
    public Class<RLockForceReleaseContext> getContextClazz() {
        return RLockForceReleaseContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RLockForceReleaseContext context) {
        return context.containsKey(RLockForceReleaseJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RLockForceReleaseContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RLockForceReleaseContext context) {
        RLockForceReleaseJFREvent rLockForceReleaseJFREvent = context.get(RLockForceReleaseJFREvent.class);
        rLockForceReleaseJFREvent.setLockReleasedSuccessfully(context.isLockReleasedSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rLockForceReleaseJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rLockForceReleaseJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rLockForceReleaseJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RLockForceReleaseContext context) {
        context.put(RLockForceReleaseJFREvent.class, new RLockForceReleaseJFREvent(context));
    }
}
