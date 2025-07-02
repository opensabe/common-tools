package io.github.opensabe.common.cache.api;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.time.Duration;

/**
 * @author heng.ma
 */
public class CaffeineExpireCacheManager extends CaffeineCacheManager implements ExpireCacheManager {

    private CaffeineSpec caffeineSpec;

    private final MultiKeyMap<Object, Cache> cacheMap = new MultiKeyMap<>();

    @Override
    public Cache getCache(String name, Duration ttl) {
        return cacheMap.computeIfAbsent( new MultiKey<>(name, ttl), k -> adaptCaffeineCache(name, Caffeine.from(caffeineSpec).expireAfterWrite(ttl).build()));
    }

    @Override
    public void setCacheLoader(CacheLoader<Object, Object> cacheLoader) {
        super.setCacheLoader(cacheLoader);
        cacheMap.clear();
    }

    @Override
    public void setAsyncCacheLoader(AsyncCacheLoader<Object, Object> cacheLoader) {
        super.setAsyncCacheLoader(cacheLoader);
        cacheMap.clear();
    }

    @Override
    public void setAsyncCacheMode(boolean asyncCacheMode) {
        super.setAsyncCacheMode(asyncCacheMode);
        cacheMap.clear();
    }

    @Override
    public void setAllowNullValues(boolean allowNullValues) {
        super.setAllowNullValues(allowNullValues);
        cacheMap.clear();
    }

    @Override
    public void setCaffeine(Caffeine<Object, Object> caffeine) {
        super.setCaffeine(caffeine);
        cacheMap.clear();
    }

    @Override
    public void setCaffeineSpec(CaffeineSpec caffeineSpec) {
        super.setCaffeineSpec(caffeineSpec);
        this.caffeineSpec = caffeineSpec;
        cacheMap.clear();
    }

    @Override
    public void setCacheSpecification(String cacheSpecification) {
        super.setCacheSpecification(cacheSpecification);
        this.caffeineSpec = CaffeineSpec.parse(cacheSpecification);
        cacheMap.clear();
    }

}
