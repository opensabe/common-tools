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

import java.time.Duration;
import java.util.Collection;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;

public interface ExpireCacheManager extends CacheManager {


    /**
     * 获取cache实例，会根据ttl动态创建cache实例（没有就创建）,
     * 不受<code>RedisCacheManager.allowRuntimeCacheCreation</code>和
     * <code>CaffeineCacheManager.dynamic</code>影响
     *
     * @param name cacheName
     * @param ttl  ttl of cache
     * @return cache instance
     */
    @NonNull
    Cache getCache(String name, Duration ttl);

    /**
     * 获取cache的配置信息
     *
     * @param name cacheName
     * @return setting of cacheName。
     * null 如果当前cache没有创建
     */
    Collection<String> settings(String name);
}
