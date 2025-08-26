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
import io.github.opensabe.common.redisson.observation.ratelimiter.RRateLimiterSetRateContext;
import io.micrometer.tracing.handler.TracingObservationHandler;

public class RRateLimiterSetRateObservationToJFRGenerator extends ObservationToJFRGenerator<RRateLimiterSetRateContext> {
    @Override
    public Class<RRateLimiterSetRateContext> getContextClazz() {
        return RRateLimiterSetRateContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(RRateLimiterSetRateContext context) {
        return context.containsKey(RRateLimiterSetRateJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(RRateLimiterSetRateContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(RRateLimiterSetRateContext context) {
        RRateLimiterSetRateJFREvent rRateLimiterSetRateJFREvent = context.get(RRateLimiterSetRateJFREvent.class);
        rRateLimiterSetRateJFREvent.setRateSetSuccessfully(context.isSetRateSuccessfully());
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (tracingContext != null) {
            rRateLimiterSetRateJFREvent.setTraceId(tracingContext.getSpan().context().traceId());
            rRateLimiterSetRateJFREvent.setSpanId(tracingContext.getSpan().context().spanId());
        }
        rRateLimiterSetRateJFREvent.commit();
    }

    @Override
    protected void generateOnStart(RRateLimiterSetRateContext context) {
        RRateLimiterSetRateJFREvent rRateLimiterSetRateJFREvent = new RRateLimiterSetRateJFREvent(context);
        rRateLimiterSetRateJFREvent.begin();
        context.put(RRateLimiterSetRateJFREvent.class, rRateLimiterSetRateJFREvent);
    }
}
