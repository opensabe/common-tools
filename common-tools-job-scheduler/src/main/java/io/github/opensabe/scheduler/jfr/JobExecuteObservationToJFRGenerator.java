package io.github.opensabe.scheduler.jfr;


import io.github.opensabe.scheduler.observation.JobExecuteContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class JobExecuteObservationToJFRGenerator extends ObservationToJFRGenerator<JobExecuteContext> {


    @Override
    public Class<JobExecuteContext> getContextClazz() {
        return JobExecuteContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(JobExecuteContext context) {
        return Boolean.TRUE;
    }

    @Override
    protected boolean shouldGenerateOnStart(JobExecuteContext context) {
        return Boolean.TRUE;
    }

    @Override
    protected void commitOnStop(JobExecuteContext context) {
        JobExecuteJFREvent jobExecuteJFREvent = context.get(JobExecuteJFREvent.class);
        jobExecuteJFREvent.setStatus(context.getStatus());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            jobExecuteJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            jobExecuteJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        jobExecuteJFREvent.commit();
    }

    @Override
    protected void generateOnStart(JobExecuteContext context) {
        context.put(JobExecuteJFREvent.class, new JobExecuteJFREvent(context));
    }


}
