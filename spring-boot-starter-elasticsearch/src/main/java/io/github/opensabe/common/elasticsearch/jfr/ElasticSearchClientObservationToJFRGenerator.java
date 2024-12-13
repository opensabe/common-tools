package io.github.opensabe.common.elasticsearch.jfr;

import io.github.opensabe.common.elasticsearch.observation.ElasticSearchClientObservationContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class ElasticSearchClientObservationToJFRGenerator extends ObservationToJFRGenerator<ElasticSearchClientObservationContext> {
    @Override
    public Class<ElasticSearchClientObservationContext> getContextClazz() {
        return ElasticSearchClientObservationContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(ElasticSearchClientObservationContext context) {
        return context.containsKey(ElasticSearchClientJfrEvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(ElasticSearchClientObservationContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(ElasticSearchClientObservationContext context) {
        ElasticSearchClientJfrEvent elasticSearchClientJfrEvent = context.get(ElasticSearchClientJfrEvent.class);
        elasticSearchClientJfrEvent.setResponse(context.getResponse());
        elasticSearchClientJfrEvent.setThrowable(context.getThrowable());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            elasticSearchClientJfrEvent.setTraceId(traceContext.traceId());
            elasticSearchClientJfrEvent.setSpanId(traceContext.spanId());
        }
        elasticSearchClientJfrEvent.commit();
    }

    @Override
    protected void generateOnStart(ElasticSearchClientObservationContext context) {
        ElasticSearchClientJfrEvent elasticSearchClientJfrEvent = new ElasticSearchClientJfrEvent(context.getUri(), context.getParams());
        elasticSearchClientJfrEvent.begin();
        context.put(ElasticSearchClientJfrEvent.class, elasticSearchClientJfrEvent);
    }
}
