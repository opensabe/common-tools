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


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 声明方法返回值写入 Redisson {@link org.redisson.api.RBucket} 缓存。
 * <p>
 * 缓存键由 {@link #prefix()} 与 {@link #name()} SpEL 表达式（或默认类名+方法名）解析。
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonBucket {

    /** 默认 Bucket 键前缀。 */
    String DEFAULT_PREFIX = "redisson:bucket:";

    /**
     * 缓存键 SpEL 表达式；为空则使用 {@code 类简单名#方法名}。
     *
     * @see org.springframework.cache.annotation.Cacheable#cacheNames()
     */
    String name() default "";

    /** 缓存键前缀，默认 {@link #DEFAULT_PREFIX}。 */
    String prefix() default DEFAULT_PREFIX;

    /** 写入时使用的 Bucket 操作类型。 */
    CacheOption option() default CacheOption.SET;

    /** 缓存 TTL 数值，默认 60。 */
    int ttl() default 60;

    /** 缓存 TTL 时间单位，默认分钟。 */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
}
