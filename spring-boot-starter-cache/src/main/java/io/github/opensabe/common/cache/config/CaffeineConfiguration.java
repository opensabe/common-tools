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
package io.github.opensabe.common.cache.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizers;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.util.StringUtils;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.opensabe.common.cache.api.CompositeCacheManager;
import io.github.opensabe.common.cache.caffeine.DynamicCaffeineCacheManager;

/**
 * 根据 {@link CachesProperties} 创建 Caffeine {@link CacheManager} 并注册到 {@link CompositeCacheManager}。
 *
 * @author heng.ma
 * @see CachesProperties
 */
@ConditionalOnClass(Caffeine.class)
public class CaffeineConfiguration implements InitializingBean {

    /**
     * 自定义缓存属性。
     */
    private final CachesProperties cachesProperties;

    /**
     * Spring Cache manager 定制器集合。
     */
    private final CacheManagerCustomizers customizers;

    /**
     * 聚合 Caffeine/Redis 后端的组合 manager。
     */
    private final CompositeCacheManager compositeCacheManager;

    /**
     * @param cachesProperties      缓存属性
     * @param customizers           manager 定制器
     * @param compositeCacheManager 组合 cache manager
     */
    public CaffeineConfiguration(CachesProperties cachesProperties, CacheManagerCustomizers customizers, CompositeCacheManager compositeCacheManager) {
        this.cachesProperties = cachesProperties;
        this.customizers = customizers;
        this.compositeCacheManager = compositeCacheManager;
    }

    /**
     * 初始化动态 Caffeine manager 及 {@code caches.custom} 中类型为 CAFFEINE 的预定义 manager，
     * 并合并注册到 {@link CompositeCacheManager}。
     */
    @Override
    public void afterPropertiesSet() {
        List<CacheManager> cacheManagers = new ArrayList<>();
        cacheManagers.add(dynamicCaffeineCacheManager());
        if (cachesProperties.isEnabled() && cachesProperties.getCustom() != null) {
            List<CacheManager> list = cachesProperties.getCustom().stream().filter(p -> CacheType.CAFFEINE.equals(p.getType()))
                    .map(this::caffeineCacheManager)
                    .toList();
            if (!list.isEmpty()) {
                cacheManagers.addAll(list);
            }
        }
        compositeCacheManager.setCacheManagers(cacheManagers);
    }

    /**
     * 创建并定制 {@link DynamicCaffeineCacheManager}，供 {@link io.github.opensabe.common.cache.api.Expire} 动态 TTL 使用。
     *
     * @return 已应用 {@link CacheManagerCustomizers} 的动态 Caffeine manager
     */
    public DynamicCaffeineCacheManager dynamicCaffeineCacheManager() {
        DynamicCaffeineCacheManager cacheManager = new DynamicCaffeineCacheManager(cachesProperties);
        return customizers.customize(cacheManager);
    }

    /**
     * 根据单条 {@link CacheProperties} 构建标准 {@link CaffeineCacheManager}（非动态 TTL）。
     *
     * @param properties 缓存属性
     * @return 已定制的 {@link CaffeineCacheManager}
     */
    private CacheManager caffeineCacheManager(CacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        if (properties != null) {
            CacheProperties.Caffeine caffeine = properties.getCaffeine();
            if (StringUtils.hasText(caffeine.getSpec())) {
                Arrays.stream(caffeine.getSpec().split(","))
                        .filter(op -> op.contains("allowNullValues"))
                        .findFirst().ifPresent(op -> cacheManager.setAllowNullValues(Boolean.parseBoolean(op.split("=")[1].trim())));
                cacheManager.setCacheSpecification(caffeine.getSpec());
            }
            if (!properties.getCacheNames().isEmpty()) {
                cacheManager.setCacheNames(properties.getCacheNames());
            }
        }
        return customizers.customize(cacheManager);
    }

}
