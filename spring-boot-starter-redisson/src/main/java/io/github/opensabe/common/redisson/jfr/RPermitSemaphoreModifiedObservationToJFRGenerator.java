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
import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreModifiedContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RPermitSemaphoreModifiedObservationToJFRGenerator extends ObservationToJFRGenerator<RPermitSemaphoreModifiedContext> {
    @Override
    public Class<RPermitSemaphoreModifiedContext> getContextClazz() {
        return RPermitSemaphoreModifiedContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RPermitSemaphoreModifiedContext context) {
        return context.containsKey(RPermitSemaphoreModifiedJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RPermitSemaphoreModifiedContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RPermitSemaphoreModifiedContext context) {
        RPermitSemaphoreModifiedJFREvent rPermitSemaphoreModifiedJFREvent = context.get(RPermitSemaphoreModifiedJFREvent.class);
        rPermitSemaphoreModifiedJFREvent.setModifiedSuccessfully(context.isModifiedSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rPermitSemaphoreModifiedJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rPermitSemaphoreModifiedJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rPermitSemaphoreModifiedJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RPermitSemaphoreModifiedContext context) {
        RPermitSemaphoreModifiedJFREvent rPermitSemaphoreModifiedJFREvent = new RPermitSemaphoreModifiedJFREvent(context);
        rPermitSemaphoreModifiedJFREvent.begin();
        context.put(RPermitSemaphoreModifiedJFREvent.class, rPermitSemaphoreModifiedJFREvent);
    }
}
