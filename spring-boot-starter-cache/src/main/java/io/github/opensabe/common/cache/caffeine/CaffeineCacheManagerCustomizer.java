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
package io.github.opensabe.common.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.caffeine.CaffeineCache;

/**
 * <code>caffeine</code>在创建缓存最后一步时，可以修改创建行为，
 * 该实例对所有通过<code>@Expire</code>创建的<code>caffeine</code>生效。
 * 包括<code>CachesProperties</code>预定义的cacheName
 * <p><b><code>caffeine</code>重复添加配置会报错，
 * 因此使用时需要注意一下{@link #createCache(String, Caffeine, boolean)}的name是否预先定义了配置
 * </b</p>
 * @author heng.ma
 */
public interface CaffeineCacheManagerCustomizer extends CacheManagerCustomizer<DynamicCaffeineCacheManager> {
    @Override
    default void customize(DynamicCaffeineCacheManager cacheManager) {
        cacheManager.onCaffeine((name, caffeine) -> createCache(name, caffeine, cacheManager.isAllowNullValues()));
    }

    /**
     * 自定义创建Cache实例，可以添加监听器，设置引用类型等
     * @param name      cacheName
     * @param caffeine  CacheBuilder
     * @return          spring cache instance of CaffeineCache
     */
    CaffeineCache createCache(String name, Caffeine<Object, Object> caffeine, boolean allowNullValues);
}
