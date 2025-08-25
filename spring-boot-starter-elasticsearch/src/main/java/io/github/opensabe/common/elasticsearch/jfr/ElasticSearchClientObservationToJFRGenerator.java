/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
