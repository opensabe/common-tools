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

import io.github.opensabe.common.redisson.observation.ratelimiter.RRateLimiterAcquireContext;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RRateLimiterAcquireObservationToJFRGenerator extends ObservationToJFRGenerator<RRateLimiterAcquireContext> {
    @Override
    public Class<RRateLimiterAcquireContext> getContextClazz() {
        return RRateLimiterAcquireContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RRateLimiterAcquireContext context) {
        return context.containsKey(RRateLimiterAcquireJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RRateLimiterAcquireContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RRateLimiterAcquireContext context) {
        RRateLimiterAcquireJFREvent rRateLimiterAcquireJFREvent = context.get(RRateLimiterAcquireJFREvent.class);
        rRateLimiterAcquireJFREvent.setAcquireSuccessfully(context.isRateLimiterAcquiredSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rRateLimiterAcquireJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rRateLimiterAcquireJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rRateLimiterAcquireJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RRateLimiterAcquireContext context) {
        RRateLimiterAcquireJFREvent rRateLimiterAcquireJFREvent = new RRateLimiterAcquireJFREvent(context);
        rRateLimiterAcquireJFREvent.begin();
        context.put(RRateLimiterAcquireJFREvent.class, rRateLimiterAcquireJFREvent);
    }
}
