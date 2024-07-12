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
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE_TYPE.withValue(context.getMode().name()),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE.withValue(String.valueOf(context.getRate())),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE_INTERVAL.withValue(String.valueOf(context.getRateInterval())),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE_INTERVAL_UNIT.withValue(context.getRateIntervalUnit().name()),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.SET_RATE_SUCCESSFULLY.withValue(String.valueOf(context.isSetRateSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RRateLimiterSetRateContext context) {
        return KeyValues.of(
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE_LIMITER_NAME.withValue(context.getRateLimiterName()),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE_TYPE.withValue(context.getMode().name()),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE.withValue(String.valueOf(context.getRate())),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE_INTERVAL.withValue(String.valueOf(context.getRateInterval())),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.RATE_INTERVAL_UNIT.withValue(context.getRateIntervalUnit().name()),
                RRateLimiterObservationDocumentation.SET_RATE_TAG.SET_RATE_SUCCESSFULLY.withValue(String.valueOf(context.isSetRateSuccessfully()))
        );
    }
}
