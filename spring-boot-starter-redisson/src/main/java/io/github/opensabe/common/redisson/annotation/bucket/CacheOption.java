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

/**
 * {@link RedissonBucket} 注解对应的 {@link org.redisson.api.RBucket} 写操作策略。
 */
public enum CacheOption {

    /** 直接写入并设置 TTL。 @see org.redisson.api.RBucket#set(Object, Duration) */
    SET,

    /** 读取旧值后写入并设置 TTL。 @see org.redisson.api.RBucket#getAndSet(Object, Duration) */
    GET_AND_SET,

    /** 读取后删除键。 @see org.redisson.api.RBucket#getAndDelete() */
    GET_AND_DELETE,

    /** 读取后更新过期时间。 @see org.redisson.api.RBucket#getAndExpire(Duration) */
    GET_AND_EXPIRE,

    /** 仅更新过期时间。 @see org.redisson.api.RBucket#expire(Duration) */
    EXPIRE,

    /** 读取后清除过期时间（持久化）。 @see org.redisson.api.RBucket#getAndClearExpire() */
    GET_AND_CLEAR_EXPIRE,

    /** 键不存在时写入。 @see org.redisson.api.RBucket#setIfAbsent(Object, Duration) */
    SET_IF_ABSENT,

    /** 键存在时覆盖写入。 @see org.redisson.api.RBucket#setIfExists(Object, Duration) */
    SET_IF_EXISTS,

    /** 写入但保留原有 TTL。 @see org.redisson.api.RBucket#setAndKeepTTL(Object) */
    KEEP_TTL,

    /** 删除键。 @see org.redisson.api.RBucket#delete() */
    DELETE,

}
