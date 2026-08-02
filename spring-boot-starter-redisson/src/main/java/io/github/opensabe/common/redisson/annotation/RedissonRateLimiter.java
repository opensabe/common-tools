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

import org.redisson.api.RateType;

/**
 * 声明基于 Redisson {@link org.redisson.api.RRateLimiter} 的方法级限流。
 * <p>
 * 可标注在方法或类上；类级注解作用于该类所有 public 方法。
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(
        {ElementType.METHOD, ElementType.TYPE}
)
public @interface RedissonRateLimiter {

    /** 默认限流器键前缀。 */
    String DEFAULT_PREFIX = "redisson:rateLimiter:";

    /**
     * 限流器名称 SpEL 表达式；与 {@link RedissonRateLimiterName} 二选一。
     * 若两者均为空字符串，则限流不生效。
     */
    String name() default "";

    /** 限流器键前缀，默认 {@link #DEFAULT_PREFIX}。 */
    String prefix() default DEFAULT_PREFIX;

    /** 单次调用需获取的 permit 数量。 */
    long permits() default 1;

    /** 获取 permit 失败时的行为，默认阻塞等待。 */
    Type type() default Type.BLOCK;

    /**
     * 限流模式（全局限流 / 按客户端限流等）。
     *
     * @see RateType
     */
    RateType rateType();

    /** 限流时间窗口长度。 */
    long rateInterval();

    /** 在 {@link #rateInterval()} 窗口内允许的最大 permit 数。 */
    long rate();

    /** {@link #rateInterval()} 的时间单位。 */
    TimeUnit rateIntervalUnit();

    /** 限流器空闲 TTL，对应 {@link org.redisson.api.RRateLimiter} keep-alive；0 表示不设置。 */
    long keepAlive() default 0L;

    /** {@link #keepAlive()} 的时间单位。 */
    TimeUnit keepAliveTimeUnit() default TimeUnit.MILLISECONDS;

    /** 仅 {@link Type#TRY} 生效：最长等待时间；负数表示不等待。 */
    long waitTime() default -1L;

    /** {@link #waitTime()} 的时间单位。 */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /** 获取 permit 失败时的处理策略。 */
    enum Type {
        /** 阻塞直到获取成功。 */
        BLOCK,
        /** 获取失败立即抛出 {@link io.github.opensabe.common.redisson.exceptions.RedissonRateLimiterException}。 */
        TRY
    }
}
