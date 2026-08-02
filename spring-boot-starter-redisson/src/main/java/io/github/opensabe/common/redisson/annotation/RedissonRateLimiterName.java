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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注方法参数以动态解析限流器名称（已废弃）。
 * <p>
 * 限流器名由 {@link #prefix()} 与参数值或 {@link #expression()} SpEL 表达式拼接而成。
 *
 * @deprecated 请改用 {@link RedissonRateLimiter#name()}
 */
@Deprecated(forRemoval = true, since = "2.0.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RedissonRateLimiterName {

    /** 限流器名前缀，默认 {@link RedissonRateLimiter#DEFAULT_PREFIX}。 */
    String prefix() default RedissonRateLimiter.DEFAULT_PREFIX;

    /** 作用于标注参数的 SpEL 模板表达式；为空则直接使用参数 {@code toString()}。 */
    String expression() default "";
}
