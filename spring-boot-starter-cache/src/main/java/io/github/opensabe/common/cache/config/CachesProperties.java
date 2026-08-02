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

import java.util.List;

import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 扩展 Spring Boot {@link CacheProperties} 的自定义缓存配置，绑定前缀 {@code caches}。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "caches")
public class CachesProperties {

    /**
     * 按类型（Caffeine、Redis 等）声明的具名缓存列表。
     */
    private List<CustomCacheProperties> custom;

    /**
     * 是否启用 {@code caches.custom} 预定义缓存；默认 {@code true}。
     */
    private boolean enabled = true;

    /**
     * 单条自定义缓存配置，继承 Boot 标准 {@link CacheProperties} 并附加描述字段。
     */
    @Getter
    @Setter
    public static class CustomCacheProperties extends CacheProperties {

        /**
         * 缓存用途说明（文档/运维标识，不参与运行时逻辑）。
         */
        private String cacheDesc;

        /**
         * 按 {@link #getType()} 返回对应后端的详细配置对象。
         *
         * @return Caffeine、Redis 等类型专属配置；未知类型时 {@code null}
         */
        public Object getCacheSetting() {
            return switch (getType()) {
                case CAFFEINE -> getCaffeine();
                case COUCHBASE -> getCouchbase();
                case INFINISPAN -> getInfinispan();
                case JCACHE -> getJcache();
                case REDIS -> getRedis();
                default -> null;
            };
        }
    }
}
