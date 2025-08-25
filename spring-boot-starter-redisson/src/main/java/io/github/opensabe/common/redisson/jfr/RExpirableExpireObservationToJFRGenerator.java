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
package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.common.redisson.observation.rexpirable.RExpirableExpireContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RExpirableExpireObservationToJFRGenerator extends ObservationToJFRGenerator<RExpirableExpireContext> {
    @Override
    public Class<RExpirableExpireContext> getContextClazz() {
        return RExpirableExpireContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RExpirableExpireContext context) {
        return context.containsKey(RExpirableExpireJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RExpirableExpireContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RExpirableExpireContext context) {
        RExpirableExpireJFREvent rExpirableExpireJFREvent = context.get(RExpirableExpireJFREvent.class);
        rExpirableExpireJFREvent.setExpireSetSuccessfully(context.isExpireSetSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            TraceContext traceContext = tracingContext.getSpan().context();
            rExpirableExpireJFREvent.setTraceId(traceContext.traceId());
            rExpirableExpireJFREvent.setSpanId(traceContext.spanId());
        }
        rExpirableExpireJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RExpirableExpireContext context) {
        RExpirableExpireJFREvent rExpirableExpireJFREvent = new RExpirableExpireJFREvent(context);
        rExpirableExpireJFREvent.begin();
        context.put(RExpirableExpireJFREvent.class, rExpirableExpireJFREvent);
    }
}