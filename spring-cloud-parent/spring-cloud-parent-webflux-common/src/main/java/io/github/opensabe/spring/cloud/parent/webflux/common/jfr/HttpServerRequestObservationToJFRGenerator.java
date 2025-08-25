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

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HttpServerRequestObservationToJFRGenerator extends ObservationToJFRGenerator<ServerRequestObservationContext> {
    private final HttpServerJFRProperties properties;

    public HttpServerRequestObservationToJFRGenerator(HttpServerJFRProperties properties) {
        this.properties = properties;
    }

    @Override
    public Class<ServerRequestObservationContext> getContextClazz() {
        return ServerRequestObservationContext.class;
    }

    private boolean shouldGenerate(ServerRequestObservationContext context) {
        return properties.isEnabled();
    }

    private boolean shouldCommit(ServerRequestObservationContext context) {
        if (!properties.isEnabled()) {
            return false;
        } else if (context.containsKey(HttpServerRequestJFREvent.class)) {
            return true;
        } else {
            log.error("HttpServerRequestObservationToJFRGenerator-shouldCommit context {} does not contain httpServerRequestJFREvent", context);
            return false;
        }
    }

    @Override
    protected boolean shouldCommitOnStop(ServerRequestObservationContext context) {
        return shouldCommit(context);
    }

    @Override
    protected boolean shouldGenerateOnStart(ServerRequestObservationContext context) {
        return shouldGenerate(context);
    }

    private void generate(ServerRequestObservationContext context) {
        ServerHttpRequest carrier = context.getCarrier();
        HttpServerRequestJFREvent httpServerRequestJFREvent = new HttpServerRequestJFREvent(carrier.getMethod().toString(), carrier.getPath().toString(), carrier.getQueryParams().toString(), carrier.getHeaders().toString());
        httpServerRequestJFREvent.begin();
        context.put(HttpServerRequestJFREvent.class, httpServerRequestJFREvent);
    }

    private void commit(ServerRequestObservationContext context) {
        HttpServerRequestJFREvent httpServerRequestJFREvent = context.get(HttpServerRequestJFREvent.class);
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            httpServerRequestJFREvent.setTraceId(traceContext.traceId());
            httpServerRequestJFREvent.setSpanId(traceContext.spanId());
        } else {
            log.error("HttpServerRequestObservationToJFRGenerator-commit context {} does not contain tracingContext", context);
        }
        ServerHttpResponse response = context.getResponse();
        if (response != null) {
            httpServerRequestJFREvent.setStatus(response.getStatusCode() == null ? 0 : response.getStatusCode().value());
            httpServerRequestJFREvent.setResponseHeaders(response.getHeaders().toString());
        }
        Throwable error = context.getError();
        if (error != null) {
            httpServerRequestJFREvent.setThrowable(error.getMessage());
        }
        httpServerRequestJFREvent.commit();
    }

    @Override
    protected void commitOnStop(ServerRequestObservationContext context) {
        commit(context);
    }

    @Override
    protected void generateOnStart(ServerRequestObservationContext context) {
        generate(context);
    }
}
