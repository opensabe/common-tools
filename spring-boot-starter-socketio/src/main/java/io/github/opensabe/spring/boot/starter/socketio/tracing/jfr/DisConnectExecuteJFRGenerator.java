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
package io.github.opensabe.spring.boot.starter.socketio.tracing.jfr;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.spring.boot.starter.socketio.tracing.EventEnum;
import io.github.opensabe.spring.boot.starter.socketio.tracing.observation.SocketIOExecuteContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

import java.util.Objects;

public class DisConnectExecuteJFRGenerator extends ObservationToJFRGenerator<SocketIOExecuteContext> {
    @Override
    public Class<SocketIOExecuteContext> getContextClazz() {
        return SocketIOExecuteContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(SocketIOExecuteContext context) {
        return context.containsKey(SocketIODisConnectEvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(SocketIOExecuteContext context) {
        return context.getEventEnum() == EventEnum.OnDisconnect;
    }

    @Override
    protected void commitOnStop(SocketIOExecuteContext context) {
        SocketIODisConnectEvent event = context.get(SocketIODisConnectEvent.class);
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (Objects.nonNull(tracingContext)) {
            TraceContext traceContext = tracingContext.getSpan().context();
            event.setTraceId(traceContext.traceId());
            event.setSpanId(traceContext.spanId());
        }
        event.commit();
    }

    @Override
    protected void generateOnStart(SocketIOExecuteContext context) {
        SocketIODisConnectEvent event = new SocketIODisConnectEvent(context);
        context.put(SocketIODisConnectEvent.class, event);
    }
}
