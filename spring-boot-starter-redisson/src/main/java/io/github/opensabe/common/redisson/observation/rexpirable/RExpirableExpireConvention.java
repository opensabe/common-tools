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
package io.github.opensabe.common.redisson.observation.rexpirable;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class RExpirableExpireConvention implements ObservationConvention<RExpirableExpireContext> {
    public static final RExpirableExpireConvention DEFAULT = new RExpirableExpireConvention();
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof RExpirableExpireContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(RExpirableExpireContext context) {
        return KeyValues.of(
                RExpirableObservationDocumentation.EXPIRE_TAG.EXPIRE_SUCCESSFULLY.withValue(String.valueOf(context.isExpireSetSuccessfully()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(RExpirableExpireContext context) {
        return KeyValues.of(
                RExpirableObservationDocumentation.EXPIRE_TAG.EXPIRE_SUCCESSFULLY.withValue(String.valueOf(context.isExpireSetSuccessfully())),
                RExpirableObservationDocumentation.EXPIRE_TAG.RATE_LIMITER_NAME.withValue(context.getExpirableName()),
                RExpirableObservationDocumentation.EXPIRE_TAG.THREAD_NAME.withValue(context.getThreadName())
        );
    }
}
