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
