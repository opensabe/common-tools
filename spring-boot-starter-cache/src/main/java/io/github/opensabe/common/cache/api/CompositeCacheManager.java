package io.github.opensabe.common.cache.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.common.cache.caffeine.DynamicCaffeineCacheManager;
import io.github.opensabe.common.cache.redis.DynamicRedisCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.util.Assert;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import java.util.*;

/**
 * 实现 <code>Expire#cacheType()</code>逻辑
 * @see Expire#cacheType()
 * @author heng.ma
 */
public class CompositeCacheManager extends org.springframework.cache.support.CompositeCacheManager implements ExpireCacheManager {

    private final List<CacheManager> cacheManagers = new ArrayList<>();

    private Integer caffeineIndex;

    private Integer redisIndex;

    public CompositeCacheManager() {
        super();
    }

    private static VarHandle caffeineCacheBuilder;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            caffeineCacheBuilder = lookup.findVarHandle(CaffeineCacheManager.class, "cacheBuilder", Caffeine.class);
            caffeineCacheBuilder.accessModeType(VarHandle.AccessMode.GET);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {

        }
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
                //优先选择预定义的cacheManager
                if (expireCacheManager.getCacheNames().contains(name)) {
                    return expireCacheManager.getCache(name, ttl);
                }
            }
        }
        //如果预定义的没有，优先使用caffeine,如果没有caffeine，使用redis
        Integer index = caffeineIndex == null? redisIndex : caffeineIndex;
        if (index != null) {
            return ((ExpireCacheManager)cacheManagers.get(index)).getCache(name, ttl);
        }
        return null;
    }

    @Override
    public Collection<String> settings (String cacheName) {
        Set<String> set = new HashSet<>();
        for (CacheManager cacheManager : cacheManagers) {
            Collection<String> settings;
            {
                switch (cacheManager) {
                    case ExpireCacheManager expireCacheManager -> settings = expireCacheManager.settings(cacheName);
                    case CaffeineCacheManager caffeineCacheManager -> {
                        Object o = caffeineCacheBuilder.get(caffeineCacheManager);
                        settings = List.of(o.toString());
                    }
                    case RedisCacheManager redisCacheManager -> {
                        RedisCacheConfiguration configuration = redisCacheManager.getCacheConfigurations().get(cacheName);
                        Duration duration = configuration.getTtlFunction().getTimeToLive(Object.class, null);
                        String keyPrefix = configuration.getKeyPrefix().compute(cacheName);
                        settings = List.of(keyPrefix, duration.toString());
                    }
                    default -> settings = Collections.emptyList();
                }
            }
            set.addAll(settings);
        }
        return set;
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
