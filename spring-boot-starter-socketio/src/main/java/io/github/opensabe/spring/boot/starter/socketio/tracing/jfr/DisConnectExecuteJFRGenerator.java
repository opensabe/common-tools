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
