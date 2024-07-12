package io.github.opensabe.common.redisson.observation.rsemaphore;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum RPermitSemaphoreObservationDocumentation implements ObservationDocumentation {
    SEMAPHORE_ACQUIRE {
        @Override
        public String getName() {
            return "redisson.semaphore.acquire";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RPermitSemaphoreAcquiredObservationConvention.class;
        }
    },
    SEMAPHORE_RELEASE {
        @Override
        public String getName() {
            return "redisson.semaphore.release";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RPermitSemaphoreReleasedObservationConvention.class;
        }
    },
    PERMIT_MODIFIED {
        @Override
        public String getName() {
            return "redisson.semaphore.permit.modified";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RPermitSemaphoreModifiedObservationConvention.class;
        }
    },
    ;

    public enum SEMAPHORE_ACQUIRE_TAG implements KeyName {
        SEMAPHORE_NAME {
            @Override
            public String asString() {
                return "redisson.semaphore.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.semaphore.acquire.thread.name";
            }
        },
        TRY_ACQUIRE {
            @Override
            public String asString() {
                return "redisson.semaphore.try.acquire";
            }
        },
        WAIT_TIME {
            @Override
            public String asString() {
                return "redisson.semaphore.wait.time";
            }
        },
        LEASE_TIME {
            @Override
            public String asString() {
                return "redisson.semaphore.lease.time";
            }
        },
        TIMEUNIT {
            @Override
            public String asString() {
                return "redisson.semaphore.timeunit";
            }
        },
        PERMIT_ID {
            @Override
            public String asString() {
                return "redisson.semaphore.permit.id";
            }
        },
        ;
    }

    public enum SEMAPHORE_RELEASE_TAG implements KeyName {
        SEMAPHORE_NAME {
            @Override
            public String asString() {
                return "redisson.semaphore.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.semaphore.release.thread.name";
            }
        },
        PERMIT_ID {
            @Override
            public String asString() {
                return "redisson.semaphore.permit.id";
            }
        },
        SEMAPHORE_RELEASED_SUCCESSFULLY {
            @Override
            public String asString() {
                return "redisson.semaphore.release.successfully";
            }
        },
        ;
    }

    public enum PERMIT_MODIFIED_TAG implements KeyName {
        SEMAPHORE_NAME {
            @Override
            public String asString() {
                return "redisson.semaphore.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.semaphore.release.thread.name";
            }
        },
        MODIFIED {
            @Override
            public String asString() {
                return "redisson.semaphore.modified";
            }
        },
        MODIFIED_SUCCESSFULLY {
            @Override
            public String asString() {
                return "redisson.semaphore.modified.successfully";
            }
        },
        ;
    }
}
