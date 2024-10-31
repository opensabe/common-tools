package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RPermitSemaphoreAcquiredObservationConvention implements ObservationConvention<RPermitSemaphoreAcquiredContext> {
    public static final RPermitSemaphoreAcquiredObservationConvention DEFAULT = new RPermitSemaphoreAcquiredObservationConvention();
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RPermitSemaphoreAcquiredContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RPermitSemaphoreAcquiredContext context) {
        return KeyValues.of(
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.TRY_ACQUIRE.withValue(String.valueOf(context.isTryAcquire()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RPermitSemaphoreAcquiredContext context) {
        return KeyValues.of(
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.SEMAPHORE_NAME.withValue(context.getSemaphoreName()),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.TRY_ACQUIRE.withValue(String.valueOf(context.isTryAcquire())),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.WAIT_TIME.withValue(String.valueOf(context.getWaitTime())),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.LEASE_TIME.withValue(String.valueOf(context.getLeaseTime())),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.TIMEUNIT.withValue(context.getUnit().name()),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_ACQUIRE_TAG.PERMIT_ID.withValue(String.valueOf(context.getPermitId()))
        );
    }
}
