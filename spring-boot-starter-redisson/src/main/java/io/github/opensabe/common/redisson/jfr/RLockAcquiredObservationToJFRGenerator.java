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
import io.github.opensabe.common.redisson.observation.rlock.RLockAcquiredContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RLockAcquiredObservationToJFRGenerator extends ObservationToJFRGenerator<RLockAcquiredContext> {
    @Override
    public Class<RLockAcquiredContext> getContextClazz() {
        return RLockAcquiredContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RLockAcquiredContext context) {
        return context.containsKey(RLockAcquiredJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RLockAcquiredContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RLockAcquiredContext context) {
        RLockAcquiredJFREvent rLockAcquiredJFREvent = context.get(RLockAcquiredJFREvent.class);
        rLockAcquiredJFREvent.setLockAcquiredSuccessfully(context.isLockAcquiredSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rLockAcquiredJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rLockAcquiredJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rLockAcquiredJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RLockAcquiredContext context) {
        RLockAcquiredJFREvent rLockAcquiredJFREvent = new RLockAcquiredJFREvent(context);
        rLockAcquiredJFREvent.begin();
        context.put(RLockAcquiredJFREvent.class, rLockAcquiredJFREvent);
    }
}
