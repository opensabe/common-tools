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
import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreReleasedContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RPermitSemaphoreReleasedObservationToJFRGenerator extends ObservationToJFRGenerator<RPermitSemaphoreReleasedContext> {

    @Override
    public Class<RPermitSemaphoreReleasedContext> getContextClazz() {
        return RPermitSemaphoreReleasedContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RPermitSemaphoreReleasedContext context) {
        return context.containsKey(RPermitSemaphoreReleasedJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RPermitSemaphoreReleasedContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RPermitSemaphoreReleasedContext context) {
        RPermitSemaphoreReleasedJFREvent rPermitSemaphoreReleasedJFREvent = context.get(RPermitSemaphoreReleasedJFREvent.class);
        rPermitSemaphoreReleasedJFREvent.setPermitReleasedSuccessfully(context.isPermitReleasedSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rPermitSemaphoreReleasedJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rPermitSemaphoreReleasedJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rPermitSemaphoreReleasedJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RPermitSemaphoreReleasedContext context) {
        RPermitSemaphoreReleasedJFREvent rPermitSemaphoreReleasedJFREvent = new RPermitSemaphoreReleasedJFREvent(context);
        rPermitSemaphoreReleasedJFREvent.begin();
        context.put(RPermitSemaphoreReleasedJFREvent.class, rPermitSemaphoreReleasedJFREvent);
    }
}
