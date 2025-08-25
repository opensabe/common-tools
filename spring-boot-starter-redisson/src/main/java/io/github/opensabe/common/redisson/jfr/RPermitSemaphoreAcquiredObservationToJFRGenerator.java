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
import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreAcquiredContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RPermitSemaphoreAcquiredObservationToJFRGenerator extends ObservationToJFRGenerator<RPermitSemaphoreAcquiredContext> {

    @Override
    public Class<RPermitSemaphoreAcquiredContext> getContextClazz() {
        return RPermitSemaphoreAcquiredContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RPermitSemaphoreAcquiredContext context) {
        return context.containsKey(RPermitSemaphoreAcquiredJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RPermitSemaphoreAcquiredContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RPermitSemaphoreAcquiredContext context) {
        RPermitSemaphoreAcquiredJFREvent rPermitSemaphoreAcquiredJFREvent = context.get(RPermitSemaphoreAcquiredJFREvent.class);
        rPermitSemaphoreAcquiredJFREvent.setPermitId(context.getPermitId());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rPermitSemaphoreAcquiredJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rPermitSemaphoreAcquiredJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rPermitSemaphoreAcquiredJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RPermitSemaphoreAcquiredContext context) {
        RPermitSemaphoreAcquiredJFREvent rPermitSemaphoreAcquiredJFREvent = new RPermitSemaphoreAcquiredJFREvent(context);
        rPermitSemaphoreAcquiredJFREvent.begin();
        context.put(RPermitSemaphoreAcquiredJFREvent.class, rPermitSemaphoreAcquiredJFREvent);
    }
}