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
package io.github.opensabe.common.redisson.observation.ratelimiter;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum RRateLimiterObservationDocumentation implements ObservationDocumentation {
    SET_RATE {
        @Override
        public String getName() {
            return "redisson.ratelimiter.set.rate";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RRateLimiterSetRateConvention.class;
        }
    },
    ACQUIRE {
        @Override
        public String getName() {
            return "redisson.ratelimiter.acquire";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RRateLimiterAcquireConvention.class;
        }
    },
    ;

    public enum SetRateTag implements KeyName {
        RATE_LIMITER_NAME {
            @Override
            public String asString() {
                return "redisson.ratelimiter.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.ratelimiter.set.rate.thread.name";
            }
        },
        RATE_TYPE {
            @Override
            public String asString() {
                return "redisson.ratelimiter.set.rate.type";
            }
        },
        RATE {
            @Override
            public String asString() {
                return "redisson.ratelimiter.set.rate";
            }
        },
        RATE_INTERVAL {
            @Override
            public String asString() {
                return "redisson.ratelimiter.set.rate.interval";
            }
        },
        RATE_INTERVAL_UNIT {
            @Override
            public String asString() {
                return "redisson.ratelimiter.set.rate.interval.unit";
            }
        },
        SET_RATE_SUCCESSFULLY {
            @Override
            public String asString() {
                return "redisson.ratelimiter.set.rate.successfully";
            }
        },
        KEEP_ALIVE {
            @Override
            public String asString() {
                return "redisson.ratelimiter.set.rate.keep.alive";
            }
        };
    }

    public enum AcquireTag implements KeyName {
        RATE_LIMITER_NAME {
            @Override
            public String asString() {
                return "redisson.ratelimiter.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.ratelimiter.acquire.thread.name";
            }
        },
        PERMITS {
            @Override
            public String asString() {
                return "redisson.ratelimiter.acquire.permits";
            }
        },
        TIME_UNIT {
            @Override
            public String asString() {
                return "redisson.ratelimiter.acquire.timeunit";
            }
        },
        TIME_OUT {
            @Override
            public String asString() {
                return "redisson.ratelimiter.acquire.time.out";
            }
        },
        ACQUIRE_SUCCESSFULLY {
            @Override
            public String asString() {
                return "redisson.ratelimiter.acquire.successfully";
            }
        },
        ;
    }
}
