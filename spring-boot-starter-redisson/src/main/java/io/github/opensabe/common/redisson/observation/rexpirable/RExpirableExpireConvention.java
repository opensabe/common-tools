package io.github.opensabe.common.redisson.observation.rexpirable;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RExpirableExpireConvention implements ObservationConvention<RExpirableExpireContext> {
    public static final RExpirableExpireConvention DEFAULT = new RExpirableExpireConvention();
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RExpirableExpireContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RExpirableExpireContext context) {
        return KeyValues.of(
                RExpirableObservationDocumentation.EXPIRE_TAG.EXPIRE_SUCCESSFULLY.withValue(String.valueOf(context.isExpireSetSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RExpirableExpireContext context) {
        return KeyValues.of(
                RExpirableObservationDocumentation.EXPIRE_TAG.EXPIRE_SUCCESSFULLY.withValue(String.valueOf(context.isExpireSetSuccessfully())),
                RExpirableObservationDocumentation.EXPIRE_TAG.RATE_LIMITER_NAME.withValue(context.getExpirableName()),
                RExpirableObservationDocumentation.EXPIRE_TAG.THREAD_NAME.withValue(context.getThreadName())
        );
    }
}
