package io.github.opensabe.common.cache.api;

import jakarta.annotation.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;

public interface ExpireCacheManager extends CacheManager {

    /**
     * 这里创建的cache不需要再用map缓存了，因为CacheResolver里已经缓存了
     * @param name  cache name
     * @param ttl   expire time
     * @return      cache with ttl
     */
    @Nullable
    Cache getCache(String name, Duration ttl);

}
