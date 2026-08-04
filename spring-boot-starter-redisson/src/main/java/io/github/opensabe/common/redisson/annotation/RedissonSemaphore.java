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
package io.github.opensabe.common.redisson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 声明基于 Redisson {@link org.redisson.api.RPermitExpirableSemaphore} 的方法级并发控制。
 * <p>
 * 底层使用可过期 permit 信号量（非普通 {@link org.redisson.api.RSemaphore}），支持 lease 自动回收。
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(
        {ElementType.METHOD, ElementType.TYPE}
)
public @interface RedissonSemaphore {

    /** 默认信号量键前缀。 */
    String DEFAULT_PREFIX = "redisson:semaphore:";

    /**
     * 信号量名称 SpEL 表达式；与 {@link RedissonSemaphoreName} 二选一。
     * 若两者均为空字符串，则信号量不生效。
     */
    String name() default "";

    /** 信号量键前缀，默认 {@link #DEFAULT_PREFIX}。 */
    String prefix() default RedissonSemaphore.DEFAULT_PREFIX;

    /** 信号量 permit 总量（首次 {@code trySetPermits} 时写入 Redis）。 */
    int totalPermits() default 100;

    /** 获取 permit 失败时的行为，默认阻塞等待。 */
    Type type() default Type.BLOCK;

    /** permit 最长持有时间；-1 表示无限持有（Redisson API 语义）。 */
    long leaseTime() default -1;

    /** 仅 {@link Type#TRY} 生效：最长等待时间；负数表示不等待。 */
    long waitTime() default -1L;

    /** {@link #waitTime()} 与 {@link #leaseTime()} 的时间单位。 */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /** 获取 permit 失败时的处理策略。 */
    enum Type {
        /** 阻塞直到获取成功。 */
        BLOCK,
        /** 获取失败立即抛出 {@link io.github.opensabe.common.redisson.exceptions.RedissonSemaphoreException}。 */
        TRY
    }
}
