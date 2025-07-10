package io.github.opensabe.common.cache.api;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Collection;

public interface ExpireCacheManager extends CacheManager {


    /**
     * 获取cache实例，会根据ttl动态创建cache实例（没有就创建）,
     * 不受<code>RedisCacheManager.allowRuntimeCacheCreation</code>和
     * <code>CaffeineCacheManager.dynamic</code>影响
     *
     * @param name  cacheName
     * @param ttl   ttl of cache
     * @return      cache instance
     */
    @NonNull
    Cache getCache(String name, Duration ttl);

    /**
     * 获取cache的配置信息
     * @param name  cacheName
     * @return  setting of cacheName。
     *          null 如果当前cache没有创建
     */
    Collection<String> settings(String name);
}
