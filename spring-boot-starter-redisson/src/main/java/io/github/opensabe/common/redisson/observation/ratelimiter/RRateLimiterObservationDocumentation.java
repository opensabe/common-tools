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

    public enum SET_RATE_TAG implements KeyName {
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
        ;
    }

    public enum ACQUIRE_TAG implements KeyName {
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
