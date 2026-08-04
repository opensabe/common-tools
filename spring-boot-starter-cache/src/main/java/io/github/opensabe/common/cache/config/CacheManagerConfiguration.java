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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizers;
import org.springframework.context.annotation.Bean;

import io.github.opensabe.common.cache.api.CompositeCacheManager;

/**
 * 组合式 {@link CompositeCacheManager} 及 Spring Cache 定制器的基础 Bean 配置。
 *
 * @author heng.ma
 */
public class CacheManagerConfiguration {

    /**
     * 聚合容器中所有 {@link CacheManagerCustomizer}，供 Caffeine/Redis 配置类统一调用。
     *
     * @param customizers 按 Spring 顺序排列的定制器提供者
     * @return {@link CacheManagerCustomizers} 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        return new CacheManagerCustomizers(customizers.orderedStream().toList());
    }

    /**
     * 注册全局唯一的 {@link CompositeCacheManager}，作为 Caffeine 与 Redis 后端的聚合入口。
     *
     * @return 新的组合 cache manager
     */
    @Bean
    public CompositeCacheManager compositeCacheManager() {
        return new CompositeCacheManager();
    }
}
