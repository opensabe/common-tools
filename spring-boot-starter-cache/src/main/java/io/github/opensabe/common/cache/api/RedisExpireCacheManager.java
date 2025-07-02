package io.github.opensabe.common.cache.api;

import jakarta.annotation.Nullable;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;

import java.time.Duration;

/**
 * @author heng.ma
 */
public class RedisExpireCacheManager extends RedisCacheManager implements ExpireCacheManager {

    private MultiKeyMap<Object, Cache> map = new MultiKeyMap<>();

    public RedisExpireCacheManager(RedisCacheWriter cacheWriter,
                                   RedisCacheConfiguration defaultCacheConfiguration,
                                   boolean allowRuntimeCacheCreation) {
        super(cacheWriter, defaultCacheConfiguration, allowRuntimeCacheCreation);
    }

    @Nullable
    @Override
    public Cache getCache(String name, Duration ttl) {
        return map.computeIfAbsent(new MultiKey<>(name, ttl), k -> {
            RedisCacheConfiguration configuration = getDefaultCacheConfiguration();
            configuration.entryTtl(ttl);
            return super.createRedisCache(name, configuration);
        });
    }
}
