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

    public enum EXPIRE_TAG implements KeyName {
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
