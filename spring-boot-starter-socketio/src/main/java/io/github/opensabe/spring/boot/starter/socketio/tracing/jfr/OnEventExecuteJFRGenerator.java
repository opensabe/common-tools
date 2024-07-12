package io.github.opensabe.spring.boot.starter.socketio.tracing.jfr;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.spring.boot.starter.socketio.tracing.EventEnum;
import io.github.opensabe.spring.boot.starter.socketio.tracing.observation.SocketIOExecuteContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

import java.util.Objects;

public class OnEventExecuteJFRGenerator extends ObservationToJFRGenerator<SocketIOExecuteContext> {
    @Override
    public Class<SocketIOExecuteContext> getContextClazz() {
        return SocketIOExecuteContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(SocketIOExecuteContext context) {
        return context.containsKey(SocketIOOnEvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(SocketIOExecuteContext context) {
        return context.getEventEnum() == EventEnum.OnEvent;
    }

    @Override
    protected void commitOnStop(SocketIOExecuteContext context) {
        SocketIOOnEvent event = context.get(SocketIOOnEvent.class);
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
        SocketIOOnEvent event = new SocketIOOnEvent(context.getSocketIOClient(),context.getEventName());
        context.put(SocketIOOnEvent.class, event);
    }
}
