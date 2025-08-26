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

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum RExpirableObservationDocumentation implements ObservationDocumentation {
    EXPIRE {
        @Override
        public String getName() {
            return "redisson.ratelimiter.expire";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RExpirableExpireConvention.class;
        }
    },
    ;

    public enum ExpireTag implements KeyName {
        RATE_LIMITER_NAME {
            @Override
            public String asString() {
                return "redisson.ratelimiter.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.ratelimiter.expire.thread.name";
            }
        },
        EXPIRE_SUCCESSFULLY {
            @Override
            public String asString() {
                return "redisson.ratelimiter.expire.successfully";
            }
        },
        ;
    }
}
