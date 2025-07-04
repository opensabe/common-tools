package io.github.opensabe.common.cache.api;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.Collection;

public interface ExpireCacheManager extends CacheManager {


    Cache getCache(String name, Duration ttl);

    /**
     * 获取cache的配置信息
     * @param name  cacheName
     * @return  setting of cacheName
     */
    Collection<String> settings(String name);
}
