package io.github.opensabe.spring.boot.starter.rocketmq.jfr;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageProduceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class MessageProduceToJFRGenerator extends ObservationToJFRGenerator<MessageProduceContext> {

    @Override
    public Class<MessageProduceContext> getContextClazz() {
        return MessageProduceContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(MessageProduceContext context) {
        return context.containsKey(MessageProduce.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(MessageProduceContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(MessageProduceContext context) {
        MessageProduce messageProduce = context.get(MessageProduce.class);
        messageProduce.setSendResult(context.getSendResult());
        messageProduce.setThrowable(context.getThrowable());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            messageProduce.setTraceId(traceContext.traceId());
            messageProduce.setSpanId(traceContext.spanId());
        }
        messageProduce.commit();
    }

    @Override
    protected void generateOnStart(MessageProduceContext context) {
        MessageProduce messageProduce = new MessageProduce(context.getTopic(), context.getMsgLength());
        messageProduce.begin();
        context.put(MessageProduce.class, messageProduce);
    }
}
