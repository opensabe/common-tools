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

/**
 * 声明方法执行后驱逐 Redisson {@link org.redisson.api.RBucket} 缓存条目。
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonBucketEvict {

    /**
     * 待驱逐缓存键 SpEL 表达式；为空则使用 {@code 类简单名#方法名}。
     *
     * @see org.springframework.cache.annotation.Cacheable#cacheNames()
     */
    String name() default "";

    /** 缓存键前缀，默认 {@link RedissonBucket#DEFAULT_PREFIX}。 */
    String prefix() default RedissonBucket.DEFAULT_PREFIX;
}
