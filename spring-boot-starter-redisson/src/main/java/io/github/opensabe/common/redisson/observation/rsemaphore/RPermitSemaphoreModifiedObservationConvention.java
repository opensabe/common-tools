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
