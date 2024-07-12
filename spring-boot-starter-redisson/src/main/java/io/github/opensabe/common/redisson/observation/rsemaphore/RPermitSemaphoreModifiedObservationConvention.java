package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RPermitSemaphoreModifiedObservationConvention implements ObservationConvention<RPermitSemaphoreModifiedContext> {
    public static final RPermitSemaphoreModifiedObservationConvention DEFAULT = new RPermitSemaphoreModifiedObservationConvention();
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RPermitSemaphoreModifiedContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RPermitSemaphoreModifiedContext context) {
        return KeyValues.of(
                RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED_TAG.MODIFIED_SUCCESSFULLY.withValue(String.valueOf(context.isModifiedSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RPermitSemaphoreModifiedContext context) {
        return KeyValues.of(
                RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED_TAG.MODIFIED_SUCCESSFULLY.withValue(String.valueOf(context.isModifiedSuccessfully())),
                RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED_TAG.SEMAPHORE_NAME.withValue(context.getSemaphoreName()),
                RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED_TAG.THREAD_NAME.withValue(context.getThreadName()),
                RPermitSemaphoreObservationDocumentation.PERMIT_MODIFIED_TAG.MODIFIED.withValue(context.getModified())
        );
    }
}
