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
