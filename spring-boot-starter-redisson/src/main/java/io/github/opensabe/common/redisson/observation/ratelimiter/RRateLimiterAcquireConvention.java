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
