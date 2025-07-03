package io.github.opensabe.common.cache.api;

import io.github.opensabe.common.cache.caffeine.DynamicCaffeineCacheManager;
import io.github.opensabe.common.cache.redis.DynamicRedisCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 *
 * </p>
 * @author heng.ma
 */
public class CompositeCacheManager extends org.springframework.cache.support.CompositeCacheManager implements ExpireCacheManager {

    private final List<CacheManager> cacheManagers = new ArrayList<>();

    private Integer caffeineIndex;

    private Integer redisIndex;

    public CompositeCacheManager() {
        super();
    }

    @Override
    public void setCacheManagers(Collection<CacheManager> cacheManagers) {
        super.setCacheManagers(cacheManagers);
        this.cacheManagers.addAll(cacheManagers);
        for (int i = 0; i < this.cacheManagers.size(); i++) {
            CacheManager cacheManager = this.cacheManagers.get(i);
            if (cacheManager instanceof DynamicCaffeineCacheManager) {
                caffeineIndex = i;
            } else if (cacheManager instanceof DynamicRedisCacheManager) {
                redisIndex = i;
            }
        }
    }

    @Override
    public Cache getCache(String name, Duration ttl) {
        for (CacheManager cacheManager : cacheManagers) {
            if (cacheManager instanceof ExpireCacheManager expireCacheManager) {
                Cache cache = expireCacheManager.getCache(name, ttl);
                if (cache != null) {
                    return cache;
                }
            }
        }

        return null;
    }

    public ExpireCacheManager redisCacheManager() {
        Assert.notNull(redisIndex, "DynamicRedisCacheManager not found");
        return (ExpireCacheManager) this.cacheManagers.get(redisIndex);
    }

    public ExpireCacheManager caffeineCacheManager() {
        Assert.notNull(caffeineIndex, "DynamicCaffeineCacheManager not found");
        return (ExpireCacheManager) this.cacheManagers.get(caffeineIndex);
    }
}
