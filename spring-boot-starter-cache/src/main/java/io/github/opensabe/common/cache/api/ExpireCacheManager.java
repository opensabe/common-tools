package io.github.opensabe.common.cache.api;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;

public interface ExpireCacheManager extends CacheManager {


    Cache getCache(String name, Duration ttl);

}
