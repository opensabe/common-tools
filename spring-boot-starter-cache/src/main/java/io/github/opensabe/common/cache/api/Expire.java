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
package io.github.opensabe.common.cache.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.cache.CacheType;

/**
 * 配合<code>@Cacheable</code>,<code>@CachePut</code>使用，指定过期时间
 *
 * @author heng.ma
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Expire {

    /**
     * 缓存过期时间,默认60秒
     */
    long value() default 60;

    /**
     * 时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 目前只能选择redis或者caffeine
     * <ol>
     *     <li>如果<code>@Cacheable</code>指定了cacheName但没有指定cacheManager会先看该cacheName有没有预先在yml定义，
     *     如果有就使用预定义的settings(ttl除外).</li>
     *     <li>如果这个cacheName没有任何预先定义，那么优先使用没有任何设置的<code>caffeine</code>。</li>
     *     <li>如果cacheManager指定的不是<code>ExpireCacheManager</code>实例会抛{@link ClassCastException}</li>
     *     <li>如果想改变这个默认的<code>caffeine</code>行为，比如添加Listener,设置最大容量等，设置loader等，</li>
     *     <li>可以通过{@link io.github.opensabe.common.cache.caffeine.CaffeineCacheManagerCustomizer}</li>
     * </ol>
     */
    CacheType cacheType() default CacheType.NONE;
}
