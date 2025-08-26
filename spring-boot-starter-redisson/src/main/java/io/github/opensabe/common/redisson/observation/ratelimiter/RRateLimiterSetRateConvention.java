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

public class RRateLimiterSetRateConvention implements ObservationConvention<RRateLimiterSetRateContext> {
    public static final RRateLimiterSetRateConvention DEFAULT = new RRateLimiterSetRateConvention();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RRateLimiterSetRateContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RRateLimiterSetRateContext context) {
        return KeyValues.of(
                RRateLimiterObservationDocumentation.SetRateTag.RATE_TYPE.withValue(context.getMode().name()),
                RRateLimiterObservationDocumentation.SetRateTag.RATE.withValue(String.valueOf(context.getRate())),
                RRateLimiterObservationDocumentation.SetRateTag.RATE_INTERVAL.withValue(String.valueOf(context.getRateInterval())),
                RRateLimiterObservationDocumentation.SetRateTag.RATE_INTERVAL_UNIT.withValue(context.getRateIntervalUnit().name()),
                RRateLimiterObservationDocumentation.SetRateTag.SET_RATE_SUCCESSFULLY.withValue(String.valueOf(context.isSetRateSuccessfully())),
                RRateLimiterObservationDocumentation.SetRateTag.KEEP_ALIVE.withValue(String.valueOf(context.getKeepAlive()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RRateLimiterSetRateContext context) {
        return KeyValues.of(
                RRateLimiterObservationDocumentation.SetRateTag.RATE_LIMITER_NAME.withValue(context.getRateLimiterName()),
                RRateLimiterObservationDocumentation.SetRateTag.THREAD_NAME.withValue(context.getThreadName()),
                RRateLimiterObservationDocumentation.SetRateTag.RATE_TYPE.withValue(context.getMode().name()),
                RRateLimiterObservationDocumentation.SetRateTag.RATE.withValue(String.valueOf(context.getRate())),
                RRateLimiterObservationDocumentation.SetRateTag.RATE_INTERVAL.withValue(String.valueOf(context.getRateInterval())),
                RRateLimiterObservationDocumentation.SetRateTag.RATE_INTERVAL_UNIT.withValue(context.getRateIntervalUnit().name()),
                RRateLimiterObservationDocumentation.SetRateTag.SET_RATE_SUCCESSFULLY.withValue(String.valueOf(context.isSetRateSuccessfully())),
                RRateLimiterObservationDocumentation.SetRateTag.KEEP_ALIVE.withValue(String.valueOf(context.getKeepAlive()))
        );
    }
}
