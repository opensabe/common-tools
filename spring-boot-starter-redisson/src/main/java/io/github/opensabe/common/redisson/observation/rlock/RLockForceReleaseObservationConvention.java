package io.github.opensabe.common.redisson.observation.rlock;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RLockForceReleaseObservationConvention implements ObservationConvention<RLockForceReleaseContext> {

    public static final RLockForceReleaseObservationConvention DEFAULT = new RLockForceReleaseObservationConvention();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RLockForceReleaseContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RLockForceReleaseContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isLockReleasedSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RLockForceReleaseContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_NAME.withValue(context.getLockName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LOCK_RELEASE_TAG.LOCK_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isLockReleasedSuccessfully()))
        );
    }
}
