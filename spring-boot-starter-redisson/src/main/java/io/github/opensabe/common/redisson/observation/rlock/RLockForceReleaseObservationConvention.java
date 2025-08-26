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

public class RLockForceReleaseObservationConvention implements ObservationConvention<RLockForceReleaseContext> {

    public static final RLockForceReleaseObservationConvention DEFAULT = new RLockForceReleaseObservationConvention();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RLockForceReleaseContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RLockForceReleaseContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LockReleaseTag.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LockReleaseTag.LOCK_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isLockReleasedSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RLockForceReleaseContext context) {
        return KeyValues.of(
                RLockObservationDocumentation.LockReleaseTag.LOCK_NAME.withValue(context.getLockName()),
                RLockObservationDocumentation.LockReleaseTag.THREAD_NAME.withValue(context.getThreadName()),
                RLockObservationDocumentation.LockReleaseTag.LOCK_TYPE.withValue(context.getLockClass().getSimpleName()),
                RLockObservationDocumentation.LockReleaseTag.LOCK_RELEASED_SUCCESSFULLY.withValue(String.valueOf(context.isLockReleasedSuccessfully()))
        );
    }
}
