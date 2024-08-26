package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreAcquiredContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RPermitSemaphoreAcquiredObservationToJFRGenerator extends ObservationToJFRGenerator<RPermitSemaphoreAcquiredContext> {

    @Override
    public Class<RPermitSemaphoreAcquiredContext> getContextClazz() {
        return RPermitSemaphoreAcquiredContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RPermitSemaphoreAcquiredContext context) {
        return context.containsKey(RPermitSemaphoreAcquiredJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RPermitSemaphoreAcquiredContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RPermitSemaphoreAcquiredContext context) {
        RPermitSemaphoreAcquiredJFREvent rPermitSemaphoreAcquiredJFREvent = context.get(RPermitSemaphoreAcquiredJFREvent.class);
        rPermitSemaphoreAcquiredJFREvent.setPermitId(context.getPermitId());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rPermitSemaphoreAcquiredJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rPermitSemaphoreAcquiredJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rPermitSemaphoreAcquiredJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RPermitSemaphoreAcquiredContext context) {
        RPermitSemaphoreAcquiredJFREvent rPermitSemaphoreAcquiredJFREvent = new RPermitSemaphoreAcquiredJFREvent(context);
        rPermitSemaphoreAcquiredJFREvent.begin();
        context.put(RPermitSemaphoreAcquiredJFREvent.class, rPermitSemaphoreAcquiredJFREvent);
    }
}