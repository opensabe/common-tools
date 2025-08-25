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
package io.github.opensabe.spring.cloud.parent.webflux.common.jfr;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientRequestObservationContext;
import org.springframework.web.reactive.function.client.ClientResponse;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class WebClientObservationToJFRGenerator extends ObservationToJFRGenerator<ClientRequestObservationContext> {
    private final WebClientJFRConfigurationProperties properties;

    public WebClientObservationToJFRGenerator(WebClientJFRConfigurationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Class<ClientRequestObservationContext> getContextClazz() {
        return ClientRequestObservationContext.class;
    }

    private boolean shouldGenerate(ClientRequestObservationContext context) {
        return properties.isEnabled();
    }


    private boolean shouldCommit(ClientRequestObservationContext context) {
        if (!properties.isEnabled()) {
            return false;
        } else if (context.containsKey(WebClientRequestJFREvent.class)) {
            return true;
        } else {
            log.error("WebClientObservationToJFRGenerator-shouldCommit context {} does not contain FeignRequestJFREvent", context);
            return false;
        }
    }

    @Override
    protected boolean shouldCommitOnStop(ClientRequestObservationContext context) {
        return shouldCommit(context);
    }

    @Override
    protected boolean shouldGenerateOnStart(ClientRequestObservationContext context) {
        return shouldGenerate(context);
    }

    private void generate(ClientRequestObservationContext context) {
        WebClientRequestJFREvent webClientRequestJFREvent = new WebClientRequestJFREvent();
        context.put(WebClientRequestJFREvent.class, webClientRequestJFREvent);
    }

    private void commit(ClientRequestObservationContext context) {
        WebClientRequestJFREvent webClientRequestJFREvent = context.get(WebClientRequestJFREvent.class);
        ClientRequest request = context.getRequest();
        if (request != null) {
            webClientRequestJFREvent.setUrl(request.url().toString());
            webClientRequestJFREvent.setHttpMethod(request.method().name());
            webClientRequestJFREvent.setRequestHeaders(request.headers().toString());
        } else {
            log.error("WebClientObservationToJFRGenerator-commit context {} does not contain request", context);
        }
        ClientResponse response = context.getResponse();
        if (response != null) {
            webClientRequestJFREvent.setStatus(response.statusCode().value());
            webClientRequestJFREvent.setResponseHeaders(response.headers().asHttpHeaders().toString());
        }
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            webClientRequestJFREvent.setTraceId(traceContext.traceId());
            webClientRequestJFREvent.setSpanId(traceContext.spanId());
        } else {
            log.error("WebClientObservationToJFRGenerator-commit context {} does not contain tracingContext", context);
        }
        Throwable throwable = context.getError();
        if (throwable != null) {
            webClientRequestJFREvent.setThrowable(throwable.toString());
        }
        webClientRequestJFREvent.commit();
    }

    @Override
    protected void commitOnStop(ClientRequestObservationContext context) {
        commit(context);
    }

    @Override
    protected void generateOnStart(ClientRequestObservationContext context) {
        generate(context);
    }
}
