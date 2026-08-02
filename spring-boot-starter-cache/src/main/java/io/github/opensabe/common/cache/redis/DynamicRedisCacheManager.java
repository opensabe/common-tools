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
package io.github.opensabe.common.cache.redis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.cache.Cache;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import io.github.opensabe.common.cache.api.CompositedCache;
import io.github.opensabe.common.cache.api.ExpireCacheManager;
import lombok.extern.log4j.Log4j2;

/**
 * 支持按 TTL 动态创建 Redis 缓存的 {@link RedisCacheManager} 实现。
 * <p>
 * 写入使用 {@code immediateWrites()}，与 Spring Data Redis 3.x 同步可见语义一致，
 * 避免 4.x + Lettuce 默认异步 put/evict 导致同线程读不到刚写入的值。
 * </p>
 *
 * @author heng.ma
 */
@Log4j2
public class DynamicRedisCacheManager extends RedisCacheManager implements ExpireCacheManager {

    /**
     * cache 名称 → (TTL → 已创建的 {@link Cache}) 的二级索引。
     */
    private final Map<String, Map<Duration, Cache>> map;

    /**
     * 在按 TTL 创建 {@link RedisCacheConfiguration} 前的后置定制函数链。
     */
    private Function<RedisCacheConfiguration, RedisCacheConfiguration> onRedisCacheConfiguration;

    /**
     * @param connectionFactory         Redis 连接工厂
     * @param defaultCacheConfiguration 默认缓存配置
     * @param configurations            预定义的 per-name 配置
     */
    public DynamicRedisCacheManager(RedisConnectionFactory connectionFactory,
                                    RedisCacheConfiguration defaultCacheConfiguration,
                                    Map<String, RedisCacheConfiguration> configurations) {
        super(RedisCacheWriter.create(connectionFactory, config -> config.immediateWrites()),
                defaultCacheConfiguration, configurations);
        this.map = new ConcurrentHashMap<>();
        this.onRedisCacheConfiguration = Function.identity();
    }

    /**
     * 按名称与 TTL 获取或懒创建 Redis 缓存实例。
     *
     * @param name 缓存名称
     * @param ttl  条目过期时间
     * @return 对应 TTL 的 {@link Cache}
     */
    @Override
    public Cache getCache(String name, Duration ttl) {
        return map.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).computeIfAbsent(ttl, k -> {
            RedisCacheConfiguration configuration = getCacheConfigurations().get(name);
            if (configuration == null) {
                configuration = getDefaultCacheConfiguration();
            }
            configuration = onRedisCacheConfiguration.apply(configuration.entryTtl(ttl));
            return super.decorateCache(new RCache(name, getCacheWriter(), configuration));
        });
    }

    /**
     * 当存在按 TTL 创建的缓存时，返回覆盖它们的 {@link CompositedCache}，
     * 以便未指定 {@link io.github.opensabe.common.cache.api.Expire} 的 {@code @CacheEvict} 仍能清理各 TTL 条目。
     * 否则返回 {@code null}，让 {@link io.github.opensabe.common.cache.api.CompositeCacheManager}
     * 继续查找专用 {@link RedisCacheManager} Bean（勿调用 {@code super.getCache}，以免 shadow 专用 manager
     * 导致 put/evict 落在不同 {@link RedisCache} 实例上）。
     *
     * @param name 缓存名称
     * @return 聚合缓存，或尚无动态实例时 {@code null}
     * @see CompositedCache
     */
    @Override
    public Cache getCache(String name) {
        Map<Duration, Cache> caches = map.get(name);
        if (caches != null && !caches.isEmpty()) {
            List<Cache> all = new ArrayList<>(caches.values());
            Cache dedicated = super.getCache(name);
            if (dedicated != null && !(dedicated instanceof CompositedCache)) {
                all.add(dedicated);
            }
            return new CompositedCache(name, all);
        }
        return null;
    }

    /**
     * 返回指定 cache 的配置描述；当前尚未实现，恒返回空列表。
     *
     * @param name 缓存名称
     * @return 配置字符串集合（待完善）
     */
    @Override
    public Collection<String> settings(String name) {
        return List.of();
    }

    /**
     * 合并初始配置中的名称与运行时动态创建的 cache 名称。
     *
     * @return 全部已知 cache 名称
     */
    @Override
    public Collection<String> getCacheNames() {
        Set<String> set = new HashSet<>();
        set.addAll(getInitialCacheConfiguration().keySet());
        set.addAll(map.keySet());
        return set;
    }

    /**
     * 供 Spring 生命周期加载已创建的 {@link RedisCache} 实例（含事务装饰 unwrap）。
     *
     * @return 当前已 materialize 的 Redis 缓存集合
     */
    @Override
    protected Collection<RedisCache> loadCaches() {
        return map.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(c -> isTransactionAware() ? ((TransactionAwareCacheDecorator) c).getTargetCache() : c)
                .map(c -> (RedisCache) c)
                .toList();
    }

    /**
     * 追加 Redis 缓存配置定制函数（与已有函数链式组合）。
     *
     * @param onRedisCacheConfiguration 配置变换
     * @return {@code this}，便于链式调用
     */
    public DynamicRedisCacheManager onRedisCacheConfiguration(Function<RedisCacheConfiguration, RedisCacheConfiguration> onRedisCacheConfiguration) {
        this.onRedisCacheConfiguration = this.onRedisCacheConfiguration.andThen(onRedisCacheConfiguration);
        return this;
    }

    /**
     * 带 debug 键日志的 {@link RedisCache} 子类。
     */
    public static class RCache extends RedisCache {

        /**
         * @param name                缓存名称
         * @param cacheWriter         Redis 写入器
         * @param cacheConfiguration  缓存配置
         */
        private RCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfiguration) {
            super(name, cacheWriter, cacheConfiguration);
        }

        /**
         * 生成 Redis 实际键并在 debug 级别记录。
         *
         * @param key 业务键，永不为 {@literal null}
         * @return 发往 Redis 的最终键字符串
         */
        @Override
        protected String createCacheKey(Object key) {
            String cacheKey = super.createCacheKey(key);
            if (log.isDebugEnabled()) {
                log.debug("Spring cache redis key: {}", cacheKey);
            }
            return cacheKey;
        }
    }

}
