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

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum RLockObservationDocumentation implements ObservationDocumentation {
    LOCK_ACQUIRE {
        @Override
        public String getName() {
            return "redisson.lock.acquire";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RLockAcquiredObservationConvention.class;
        }
    },
    LOCK_RELEASE {
        @Override
        public String getName() {
            return "redisson.lock.release";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RLockReleaseObservationConvention.class;
        }
    },
    LOCK_FORCE_RELEASE {
        @Override
        public String getName() {
            return "redisson.lock.release.force";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return RLockForceReleaseObservationConvention.class;
        }
    },
    ;

    public enum LOCK_ACQUIRE_TAG implements KeyName {
        LOCK_NAME {
            @Override
            public String asString() {
                return "redisson.lock.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.lock.acquire.thread.name";
            }
        },
        LOCK_ACQUIRED_SUCCESSFULLY {
            @Override
            public String asString() {
                return "redisson.lock.acquired";
            }
        },
        LOCK_TYPE {
            @Override
            public String asString() {
                return "redisson.lock.type";
            }
        },
        ;
    }

    public enum LOCK_RELEASE_TAG implements KeyName {
        LOCK_NAME {
            @Override
            public String asString() {
                return "redisson.lock.name";
            }
        },
        THREAD_NAME {
            @Override
            public String asString() {
                return "redisson.lock.release.thread.name";
            }
        },
        LOCK_RELEASED_SUCCESSFULLY {
            @Override
            public String asString() {
                return "redisson.lock.released";
            }
        },
        LOCK_TYPE {
            @Override
            public String asString() {
                return "redisson.lock.type";
            }
        },
        ;
    }
}
