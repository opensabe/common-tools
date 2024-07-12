package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RPermitSemaphoreReleasedObservationConvention implements ObservationConvention<RPermitSemaphoreReleasedContext> {
    public static final RPermitSemaphoreReleasedObservationConvention DEFAULT = new RPermitSemaphoreReleasedObservationConvention();
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RPermitSemaphoreReleasedContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RPermitSemaphoreReleasedContext context) {
        return KeyValues.of(
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE_TAG.SEMAPHORE_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isPermitReleasedSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RPermitSemaphoreReleasedContext context) {
        return KeyValues.of(
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE_TAG.SEMAPHORE_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isPermitReleasedSuccessfully())),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE_TAG.SEMAPHORE_NAME.withValue(context.getSemaphoreName()),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RPermitSemaphoreObservationDocumentation.SEMAPHORE_RELEASE_TAG.PERMIT_ID.withValue(String.valueOf(context.getPermitId()))
        );
    }
}
