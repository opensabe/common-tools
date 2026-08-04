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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.Assert;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.opensabe.common.cache.caffeine.DynamicCaffeineCacheManager;
import io.github.opensabe.common.cache.redis.DynamicRedisCacheManager;

/**
 * 组合式 {@link CacheManager}，按 {@link Expire#cacheType()} 与预定义 cache 名称路由到
 * Caffeine 或 Redis 后端，并实现 {@link ExpireCacheManager} 的动态 TTL 创建逻辑。
 *
 * @author heng.ma
 * @see Expire#cacheType()
 */
public class CompositeCacheManager extends org.springframework.cache.support.CompositeCacheManager implements ExpireCacheManager {

    /**
     * 反射访问 {@link CaffeineCacheManager} 内部 {@code cacheBuilder} 字段的 VarHandle，
     * 用于在 {@link #settings(String)} 中读取 Caffeine 构建参数。
     */
    private static VarHandle caffeineCacheBuilder;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            caffeineCacheBuilder = lookup.findVarHandle(CaffeineCacheManager.class, "cacheBuilder", Caffeine.class);
            caffeineCacheBuilder.accessModeType(VarHandle.AccessMode.GET);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {

        }
    }

    /**
     * 本类维护的 {@link CacheManager} 列表。不依赖父类 {@code cacheManagers} 字段：
     * Spring 的 {@code setCacheManagers} 仅 {@code addAll} 且从不清空，若每次 Caffeine/Redis
     * 初始化都调用 {@code super.setCacheManagers} 会导致 manager 重复注册。
     */
    private final List<CacheManager> cacheManagers = new ArrayList<>();

    /**
     * {@link DynamicCaffeineCacheManager} 在 {@link #cacheManagers} 中的下标；未注册时为 {@code null}。
     */
    private Integer caffeineIndex;

    /**
     * {@link DynamicRedisCacheManager} 在 {@link #cacheManagers} 中的下标；未注册时为 {@code null}。
     */
    private Integer redisIndex;

    /**
     * 构造空的组合缓存管理器。
     */
    public CompositeCacheManager() {
        super();
    }

    /**
     * 合并注册多个 {@link CacheManager}，并重新解析 Caffeine/Redis 动态 manager 的下标。
     * {@link io.github.opensabe.common.cache.config.CaffeineConfiguration} 与
     * {@link io.github.opensabe.common.cache.config.RedisConfiguration} 均会调用此方法，
     * 采用合并而非替换，以保证两种后端对 {@code CacheInterceptor} 均可见。
     *
     * @param cacheManagers 待合并的 cache manager 集合
     */
    @Override
    public void setCacheManagers(Collection<CacheManager> cacheManagers) {
        for (CacheManager cacheManager : cacheManagers) {
            if (!this.cacheManagers.contains(cacheManager)) {
                this.cacheManagers.add(cacheManager);
            }
        }
        this.caffeineIndex = null;
        this.redisIndex = null;
        for (int i = 0; i < this.cacheManagers.size(); i++) {
            CacheManager cacheManager = this.cacheManagers.get(i);
            if (cacheManager instanceof DynamicCaffeineCacheManager) {
                caffeineIndex = i;
            } else if (cacheManager instanceof DynamicRedisCacheManager) {
                redisIndex = i;
            }
        }
    }

    /**
     * 按注册顺序查找第一个能返回非空 {@link Cache} 的 manager。
     *
     * @param name 缓存名称
     * @return 匹配的缓存实例，若无则 {@code null}
     */
    @Override
    public Cache getCache(String name) {
        for (CacheManager cacheManager : this.cacheManagers) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                return cache;
            }
        }
        return null;
    }

    /**
     * 聚合所有已注册 manager 的 cache 名称。
     *
     * @return 不可变的 cache 名称集合
     */
    @Override
    public Collection<String> getCacheNames() {
        Set<String> names = new LinkedHashSet<>();
        for (CacheManager cacheManager : this.cacheManagers) {
            names.addAll(cacheManager.getCacheNames());
        }
        return Collections.unmodifiableSet(names);
    }

    /**
     * 按 TTL 获取或动态创建缓存。优先选择已在预定义名称列表中包含 {@code name} 的
     * {@link ExpireCacheManager}；否则回退到 Caffeine（若存在），再回退到 Redis。
     *
     * @param name 缓存名称
     * @param ttl  条目过期时间
     * @return 对应 TTL 的缓存实例，若无可用后端则 {@code null}
     */
    @Override
    public Cache getCache(String name, Duration ttl) {
        for (CacheManager cacheManager : cacheManagers) {
            if (cacheManager instanceof ExpireCacheManager expireCacheManager) {
                if (expireCacheManager.getCacheNames().contains(name)) {
                    return expireCacheManager.getCache(name, ttl);
                }
            }
        }
        Integer index = caffeineIndex == null ? redisIndex : caffeineIndex;
        if (index != null) {
            return ((ExpireCacheManager) cacheManagers.get(index)).getCache(name, ttl);
        }
        return null;
    }

    /**
     * 汇总指定 cache 名称在各 manager 中的配置描述（Caffeine builder、Redis TTL/前缀等）。
     *
     * @param cacheName 缓存名称
     * @return 去重后的配置字符串集合
     */
    @Override
    public Collection<String> settings(String cacheName) {
        Set<String> set = new HashSet<>();
        for (CacheManager cacheManager : cacheManagers) {
            Collection<String> settings;
            if (cacheManager instanceof ExpireCacheManager expireCacheManager) {
                settings = expireCacheManager.settings(cacheName);
            } else if (cacheManager instanceof CaffeineCacheManager caffeineCacheManager) {
                Object o = caffeineCacheBuilder.get(caffeineCacheManager);
                settings = List.of(o.toString());
            } else if (cacheManager instanceof RedisCacheManager redisCacheManager) {
                RedisCacheConfiguration configuration = redisCacheManager.getCacheConfigurations().get(cacheName);
                if (configuration == null) {
                    settings = Collections.emptyList();
                } else {
                    Duration duration = configuration.getTtlFunction().getTimeToLive(Object.class, null);
                    String keyPrefix = configuration.getKeyPrefix().compute(cacheName);
                    settings = List.of(keyPrefix, duration.toString());
                }
            } else {
                settings = Collections.emptyList();
            }
            set.addAll(settings);
        }
        return set;
    }

    /**
     * 返回已注册的 {@link DynamicRedisCacheManager}（包装为 {@link ExpireCacheManager}）。
     *
     * @return Redis 动态 cache manager
     * @throws IllegalArgumentException 未注册 Redis 动态 manager 时
     */
    public ExpireCacheManager redisCacheManager() {
        Assert.notNull(redisIndex, "DynamicRedisCacheManager not found");
        return (ExpireCacheManager) this.cacheManagers.get(redisIndex);
    }

    /**
     * 返回已注册的 {@link DynamicCaffeineCacheManager}（包装为 {@link ExpireCacheManager}）。
     *
     * @return Caffeine 动态 cache manager
     * @throws IllegalArgumentException 未注册 Caffeine 动态 manager 时
     */
    public ExpireCacheManager caffeineCacheManager() {
        Assert.notNull(caffeineIndex, "DynamicCaffeineCacheManager not found");
        return (ExpireCacheManager) this.cacheManagers.get(caffeineIndex);
    }
}
