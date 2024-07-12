package io.github.opensabe.spring.cloud.parent.web.common.jfr;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import feign.Request;
import feign.Response;
import feign.micrometer.FeignContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FeignObservationToJFRGenerator extends ObservationToJFRGenerator<FeignContext> {
    private final FeignJFRProperties properties;

    public FeignObservationToJFRGenerator(FeignJFRProperties properties) {
        this.properties = properties;
    }

    @Override
    public Class<FeignContext> getContextClazz() {
        return FeignContext.class;
    }

    private boolean shouldGenerate(FeignContext context) {
        return properties.isEnabled();
    }

    private boolean shouldCommit(FeignContext context) {
        if (!properties.isEnabled()) {
            return false;
        } else if (context.containsKey(FeignRequestJFREvent.class)) {
            return true;
        } else {
            log.error("FeignObservationToJFRGenerator-shouldCommit context {} does not contain FeignRequestJFREvent", context);
            return false;
        }
    }

    @Override
    protected boolean shouldCommitOnStop(FeignContext context) {
        return shouldCommit(context);
    }

    @Override
    protected boolean shouldGenerateOnStart(FeignContext context) {
        return shouldGenerate(context);
    }

    private void commit(FeignContext context) {
        FeignRequestJFREvent feignRequestJFREvent = context.get(FeignRequestJFREvent.class);
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            feignRequestJFREvent.setTraceId(traceContext.traceId());
            feignRequestJFREvent.setSpanId(traceContext.spanId());
        } else {
            log.error("FeignObservationToJFRGenerator-commitOnStop context {} does not contain TracingContext", context);
        }
        Response response = context.getResponse();
        if (response != null) {
            feignRequestJFREvent.setStatus(response.status());
            feignRequestJFREvent.setReason(response.reason());
            feignRequestJFREvent.setResponseHeaders(response.headers().toString());
        }
        Throwable error = context.getError();
        if (error != null) {
            feignRequestJFREvent.setThrowable(error.getMessage());
        }
        feignRequestJFREvent.commit();
    }

    private void generate(FeignContext context) {
        Request request = context.getCarrier();
        FeignRequestJFREvent feignRequestJFREvent = new FeignRequestJFREvent(
                request.httpMethod().name(),
                request.url(),
                request.headers().toString()
        );
        context.put(FeignRequestJFREvent.class, feignRequestJFREvent);
        feignRequestJFREvent.begin();
    }

    @Override
    protected void commitOnStop(FeignContext context) {
        commit(context);
    }

    @Override
    protected void generateOnStart(FeignContext context) {
        generate(context);
    }
}
