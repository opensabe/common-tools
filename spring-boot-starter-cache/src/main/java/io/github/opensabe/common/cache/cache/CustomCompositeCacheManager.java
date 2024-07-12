package io.github.opensabe.common.cache.cache;

import io.github.opensabe.common.cache.utils.CacheHelper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.CompositeCacheManager;

public class CustomCompositeCacheManager extends CompositeCacheManager {

    public CustomCompositeCacheManager(CacheManager... cacheManagers) {
        super(cacheManagers);
    }

    @Override
    public Cache getCache(String name) {
        if(!name.contains(CacheHelper.CACHE_NAME_PREFIX)) {
            name = CacheHelper.CACHE_NAME_PREFIX + name;
        }
        return super.getCache(name);
    }
}
