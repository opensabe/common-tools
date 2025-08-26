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
package io.github.opensabe.common.redisson.annotation.bucket;


import java.time.Duration;
import java.time.Instant;

public enum CacheOption {

    /**
     * @see org.redisson.api.RBucket#set(Object, Duration)
     */
    SET,

    /**
     * @see org.redisson.api.RBucket#getAndSet(Object, Duration)
     */
    GET_AND_SET,

    /**
     * @see org.redisson.api.RBucket#getAndDelete()
     */
    GET_AND_DELETE,

    /**
     * @see org.redisson.api.RBucket#getAndExpire(Duration)
     * @see org.redisson.api.RBucket#getAndExpire(Instant)
     */
    GET_AND_EXPIRE,

    /**
     * @see org.redisson.api.RBucket#expire(Duration)
     * @see org.redisson.api.RBucket#expire(Instant)
     */
    EXPIRE,
    /**
     * @see org.redisson.api.RBucket#getAndClearExpire()
     */
    GET_AND_CLEAR_EXPIRE,

    /**
     * @see org.redisson.api.RBucket#setIfAbsent(Object, Duration)
     */
    SET_IF_ABSENT,

    /**
     * @see org.redisson.api.RBucket#setIfExists(Object, Duration)
     */
    SET_IF_EXISTS,

    /**
     * @see org.redisson.api.RBucket#setAndKeepTTL(Object)
     */
    KEEP_TTL,

    /**
     * @see org.redisson.api.RBucket#delete()
     */
    DELETE,

}
