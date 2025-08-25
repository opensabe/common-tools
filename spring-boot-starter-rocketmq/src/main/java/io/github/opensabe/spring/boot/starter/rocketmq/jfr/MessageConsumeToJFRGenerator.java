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
import io.github.opensabe.common.redisson.jfr.RExpirableExpireJFREvent;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageConsumeContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

public class MessageConsumeToJFRGenerator extends ObservationToJFRGenerator<MessageConsumeContext> {

    @Override
    public Class<MessageConsumeContext> getContextClazz() {
        return MessageConsumeContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(MessageConsumeContext context) {
        return context.containsKey(MessageConsume.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(MessageConsumeContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(MessageConsumeContext context) {
        MessageConsume messageConsume = context.get(MessageConsume.class);
        messageConsume.setSuccessful(context.getSuccessful());
        messageConsume.setThrowable(context.getThrowable());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            messageConsume.setTraceId(traceContext.traceId());
            messageConsume.setSpanId(traceContext.spanId());
        }
        messageConsume.commit();
    }

    @Override
    protected void generateOnStart(MessageConsumeContext context) {
        MessageConsume messageConsume = new MessageConsume(context.getOriginTraceId(), context.getTopic());
        messageConsume.begin();
        context.put(MessageConsume.class, messageConsume);
    }
}
