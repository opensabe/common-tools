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

import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.caffeine.CaffeineCache;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * {@link DynamicCaffeineCacheManager} 的 Caffeine 构建定制扩展点。
 * <p>
 * 在创建缓存的最后一步可修改 Caffeine 行为；对所有通过 {@link io.github.opensabe.common.cache.api.Expire}
 * 创建的 Caffeine 缓存生效，包括 {@link io.github.opensabe.common.cache.config.CachesProperties} 预定义的 cache 名称。
 * </p>
 * <p><b>注意：Caffeine 重复添加相同配置会报错，调用 {@link #createCache(String, Caffeine, boolean)}
 * 时需确认 {@code name} 是否已在属性中预定义过 spec。</b></p>
 *
 * @author heng.ma
 */
public interface CaffeineCacheManagerCustomizer extends CacheManagerCustomizer<DynamicCaffeineCacheManager> {

    /**
     * 向动态 manager 注册 {@link #createCache(String, Caffeine, boolean)} 回调。
     *
     * @param cacheManager 待定制的 {@link DynamicCaffeineCacheManager}
     */
    @Override
    default void customize(DynamicCaffeineCacheManager cacheManager) {
        cacheManager.onCaffeine((name, caffeine) -> createCache(name, caffeine, cacheManager.isAllowNullValues()));
    }

    /**
     * 自定义创建 {@link CaffeineCache} 实例，可添加监听器、设置引用类型等。
     *
     * @param name             缓存名称
     * @param caffeine         Caffeine 构建器
     * @param allowNullValues  是否缓存 {@code null} 值
     * @return Spring {@link CaffeineCache} 实例
     */
    CaffeineCache createCache(String name, Caffeine<Object, Object> caffeine, boolean allowNullValues);
}
