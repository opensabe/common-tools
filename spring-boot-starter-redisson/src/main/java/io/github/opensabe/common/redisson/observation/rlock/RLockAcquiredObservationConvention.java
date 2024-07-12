package io.github.opensabe.common.redisson.observation.rlock;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RLockAcquiredObservationConvention implements ObservationConvention<RLockAcquiredContext> {

    public static final RLockAcquiredObservationConvention DEFAULT = new RLockAcquiredObservationConvention();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RLockAcquiredContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RLockAcquiredContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LOCK_ACQUIRE_TAG.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LOCK_ACQUIRE_TAG.LOCK_ACQUIRED_SUCCESSFULLY.withValue(String.valueOf(context.isLockAcquiredSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RLockAcquiredContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LOCK_ACQUIRE_TAG.LOCK_NAME.withValue(context.getLockName()),
                RLockObservationDocumentation.LOCK_ACQUIRE_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RLockObservationDocumentation.LOCK_ACQUIRE_TAG.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LOCK_ACQUIRE_TAG.LOCK_ACQUIRED_SUCCESSFULLY.withValue(String.valueOf(context.isLockAcquiredSuccessfully()))
        );
    }
}
