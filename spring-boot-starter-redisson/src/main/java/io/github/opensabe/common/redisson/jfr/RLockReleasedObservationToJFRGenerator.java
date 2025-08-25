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

import io.github.opensabe.common.redisson.observation.rlock.RLockReleaseContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RLockReleasedObservationToJFRGenerator extends ObservationToJFRGenerator<RLockReleaseContext> {
    @Override
    public Class<RLockReleaseContext> getContextClazz() {
        return RLockReleaseContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RLockReleaseContext context) {
        return context.containsKey(RLockReleasedJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RLockReleaseContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RLockReleaseContext context) {
        RLockReleasedJFREvent rLockReleasedJFREvent = context.get(RLockReleasedJFREvent.class);
        rLockReleasedJFREvent.setLockReleasedSuccessfully(context.isLockReleasedSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rLockReleasedJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rLockReleasedJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rLockReleasedJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RLockReleaseContext context) {
        RLockReleasedJFREvent rLockReleasedJFREvent = new RLockReleasedJFREvent(context);
        rLockReleasedJFREvent.begin();
        context.put(RLockReleasedJFREvent.class, rLockReleasedJFREvent);
    }
}
