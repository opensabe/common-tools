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
                RLockObservationDocumentation.LockAcquireTag.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LockAcquireTag.LOCK_ACQUIRED_SUCCESSFULLY.withValue(String.valueOf(context.isLockAcquiredSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RLockAcquiredContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LockAcquireTag.LOCK_NAME.withValue(context.getLockName()),
                RLockObservationDocumentation.LockAcquireTag.THREAD_NAME.withValue(context.getThreadName()),
                RLockObservationDocumentation.LockAcquireTag.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LockAcquireTag.LOCK_ACQUIRED_SUCCESSFULLY.withValue(String.valueOf(context.isLockAcquiredSuccessfully()))
        );
    }
}
