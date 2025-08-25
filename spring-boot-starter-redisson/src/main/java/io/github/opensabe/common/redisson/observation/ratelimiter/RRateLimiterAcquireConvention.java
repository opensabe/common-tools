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
package io.github.opensabe.common.redisson.observation.ratelimiter;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RRateLimiterAcquireConvention implements ObservationConvention<RRateLimiterAcquireContext> {
    public static final RRateLimiterAcquireConvention DEFAULT = new RRateLimiterAcquireConvention();
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RRateLimiterAcquireContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RRateLimiterAcquireContext context) {
        return KeyValues.of(
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.ACQUIRE_SUCCESSFULLY.withValue(String.valueOf(context.isRateLimiterAcquiredSuccessfully())),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.PERMITS.withValue(String.valueOf(context.getPermits())),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.TIME_OUT.withValue(String.valueOf(context.getTimeout())),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.TIME_UNIT.withValue(String.valueOf(context.getTimeUnit()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RRateLimiterAcquireContext context) {
        return KeyValues.of(
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.ACQUIRE_SUCCESSFULLY.withValue(String.valueOf(context.isRateLimiterAcquiredSuccessfully())),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.RATE_LIMITER_NAME.withValue(context.getRateLimiterName()),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.PERMITS.withValue(String.valueOf(context.getPermits())),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.TIME_OUT.withValue(String.valueOf(context.getTimeout())),
                RRateLimiterObservationDocumentation.ACQUIRE_TAG.TIME_UNIT.withValue(String.valueOf(context.getTimeUnit()))
        );
    }
}
