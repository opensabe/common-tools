package io.github.opensabe.common.cache.cache;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;

public interface CustomCache {

    CacheManager cacheManager(CacheProperties cacheProperties);
}
