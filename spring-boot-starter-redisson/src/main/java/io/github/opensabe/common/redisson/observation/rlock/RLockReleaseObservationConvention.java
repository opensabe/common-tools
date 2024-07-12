package io.github.opensabe.common.redisson.observation.rlock;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RLockReleaseObservationConvention implements ObservationConvention<RLockReleaseContext> {

    public static final RLockReleaseObservationConvention DEFAULT = new RLockReleaseObservationConvention();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RLockReleaseContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RLockReleaseContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isLockReleasedSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RLockReleaseContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_NAME.withValue(context.getLockName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isLockReleasedSuccessfully()))
        );
    }
}
