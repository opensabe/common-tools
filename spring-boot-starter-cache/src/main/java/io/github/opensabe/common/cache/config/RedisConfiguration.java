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

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizers;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;

import io.github.opensabe.common.cache.api.CompositeCacheManager;
import io.github.opensabe.common.cache.redis.DynamicRedisCacheManager;

/**
 * 根据 {@link CachesProperties} 创建 Redis {@link CacheManager} 并注册到 {@link CompositeCacheManager}。
 *
 * @author heng.ma
 * @see CachesProperties
 */
public class RedisConfiguration implements InitializingBean {

    /**
     * 未显式配置 key 前缀时，Redis 缓存键的默认前缀。
     */
    public static final String DEFAULT_REDIS_KEY_PREFIX = "sfccmr:";

    /**
     * Spring Cache manager 定制器集合。
     */
    private final CacheManagerCustomizers customizers;

    /**
     * Redis 连接工厂。
     */
    private final RedisConnectionFactory connectionFactory;

    /**
     * 自定义缓存属性。
     */
    private final CachesProperties properties;

    /**
     * 聚合 Caffeine/Redis 后端的组合 manager。
     */
    private final CompositeCacheManager compositeCacheManager;

    /**
     * @param customizers           manager 定制器
     * @param connectionFactory     Redis 连接
     * @param properties            缓存属性
     * @param compositeCacheManager 组合 cache manager
     */
    public RedisConfiguration(CacheManagerCustomizers customizers, RedisConnectionFactory connectionFactory, CachesProperties properties, CompositeCacheManager compositeCacheManager) {
        this.customizers = customizers;
        this.connectionFactory = connectionFactory;
        this.properties = properties;
        this.compositeCacheManager = compositeCacheManager;
    }

    /**
     * 初始化动态 Redis manager 及 {@code caches.custom} 中类型为 REDIS 的预定义 manager，
     * 并合并注册到 {@link CompositeCacheManager}。
     */
    @Override
    public void afterPropertiesSet() {
        List<CacheManager> cacheManagers = new ArrayList<>();
        cacheManagers.add(dynamicRedisCacheManager());
        if (properties.isEnabled() && properties.getCustom() != null) {
            List<CacheManager> list = properties.getCustom().stream().filter(p -> CacheType.REDIS.equals(p.getType()))
                    .map(p -> redisCacheManager(connectionFactory, customizers, p))
                    .toList();
            if (!list.isEmpty()) {
                cacheManagers.addAll(list);
            }
        }
        compositeCacheManager.setCacheManagers(cacheManagers);
    }

    /**
     * 构建带 Jackson 2 JSON 序列化与默认 key 前缀的 Redis 缓存配置。
     *
     * @return 默认 {@link RedisCacheConfiguration}
     */
    private RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .prefixCacheNameWith(DEFAULT_REDIS_KEY_PREFIX);
    }

    /**
     * 创建并定制 {@link DynamicRedisCacheManager}，预加载 {@code caches.custom} 中 REDIS 条目的 per-name 配置。
     *
     * @return 已应用 {@link CacheManagerCustomizers} 的动态 Redis manager
     */
    private DynamicRedisCacheManager dynamicRedisCacheManager() {
        Map<String, RedisCacheConfiguration> map = new HashMap<>();
        List<CachesProperties.CustomCacheProperties> list = properties.getCustom();
        if (list != null) {
            list.stream().filter(p -> CacheType.REDIS.equals(p.getType()))
                    .forEach(p -> p.getCacheNames().forEach(n -> map.put(n, configuration(p.getRedis()))));
        }
        return customizers.customize(new DynamicRedisCacheManager(connectionFactory, defaultCacheConfig(), map));
    }

    /**
     * 由 Boot {@link CacheProperties.Redis} 片段构建 {@link RedisCacheConfiguration}。
     *
     * @param redis Redis 缓存属性
     * @return 合并 TTL、前缀与 null 值策略后的配置
     */
    private RedisCacheConfiguration configuration(CacheProperties.Redis redis) {
        Duration timeToLive = redis.getTimeToLive();
        RedisCacheConfiguration configuration = defaultCacheConfig();
        if (timeToLive != null) {
            configuration = configuration.entryTtl(timeToLive);
        }
        String keyPrefix = redis.getKeyPrefix();
        if (StringUtils.hasText(keyPrefix)) {
            configuration = configuration.prefixCacheNameWith(keyPrefix);
        }
        if (!redis.isCacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }
        if (!redis.isUseKeyPrefix()) {
            configuration = configuration.disableKeyPrefix();
        }
        return configuration;
    }

    /**
     * 根据单条 {@link CacheProperties} 构建固定 TTL 的 {@link RedisCacheManager}（非动态 TTL）。
     *
     * @param redisConnectionFactory Redis 连接
     * @param customizers            manager 定制器
     * @param properties             缓存属性
     * @return 已初始化并定制的 {@link RedisCacheManager}
     */
    private CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory, CacheManagerCustomizers customizers, CacheProperties properties) {
        CacheProperties.Redis redis = properties.getRedis();
        RedisCacheConfiguration configuration = configuration(redis);
        // Spring Data Redis 4.x defaults to async writes with Lettuce; restore sync visibility.
        RedisCacheWriter cacheWriter = RedisCacheWriter.create(
                redisConnectionFactory, config -> config.immediateWrites());
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .builder(cacheWriter)
                .cacheDefaults(configuration)
                .disableCreateOnMissingCache();

        if (!properties.getCacheNames().isEmpty()) {
            builder = builder.initialCacheNames(Sets.newHashSet(properties.getCacheNames()));
        }
        RedisCacheManager customize = customizers.customize(builder.build());
        customize.afterPropertiesSet();
        return customize;
    }
}
