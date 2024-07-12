package io.github.opensabe.common.s3.jfr;

import io.github.opensabe.common.s3.observation.S3OperationContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class S3OperationObservationToJFRGenerator extends ObservationToJFRGenerator<S3OperationContext> {

    @Override
    public Class<S3OperationContext> getContextClazz() {
        return S3OperationContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(S3OperationContext context) {
        return context.containsKey(S3OperationJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(S3OperationContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(S3OperationContext context) {
        S3OperationJFREvent s3OperationJFREvent = context.get(S3OperationJFREvent.class);
        s3OperationJFREvent.setSuccess(context.isSuccess());
        s3OperationJFREvent.setFileSize(context.getFileSize());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            s3OperationJFREvent.setTraceId(traceContext.traceId());
            s3OperationJFREvent.setSpanId(traceContext.spanId());
        }
        s3OperationJFREvent.commit();
    }

    @Override
    protected void generateOnStart(S3OperationContext context) {
        S3OperationJFREvent jfrEvent = new S3OperationJFREvent(context);
        jfrEvent.begin();
        context.put(S3OperationJFREvent.class, jfrEvent);
    }
}
