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
import io.github.opensabe.common.redisson.observation.rlock.RLockForceReleaseContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RLockForceReleaseObservationToJFRGenerator extends ObservationToJFRGenerator<RLockForceReleaseContext> {
    @Override
    public Class<RLockForceReleaseContext> getContextClazz() {
        return RLockForceReleaseContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RLockForceReleaseContext context) {
        return context.containsKey(RLockForceReleaseJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RLockForceReleaseContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RLockForceReleaseContext context) {
        RLockForceReleaseJFREvent rLockForceReleaseJFREvent = context.get(RLockForceReleaseJFREvent.class);
        rLockForceReleaseJFREvent.setLockReleasedSuccessfully(context.isLockReleasedSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rLockForceReleaseJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rLockForceReleaseJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rLockForceReleaseJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RLockForceReleaseContext context) {
        RLockForceReleaseJFREvent rLockForceReleaseJFREvent = new RLockForceReleaseJFREvent(context);
        rLockForceReleaseJFREvent.begin();
        context.put(RLockForceReleaseJFREvent.class, rLockForceReleaseJFREvent);
    }
}
