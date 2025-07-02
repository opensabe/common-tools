package io.github.opensabe.common.cache.api;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

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


    public CompositeCacheManager(List<CacheManager> cacheManagers) {
        super();
        setCacheManagers(cacheManagers);
    }

    @Override
    public void setCacheManagers(Collection<CacheManager> cacheManagers) {
        super.setCacheManagers(cacheManagers);
        this.cacheManagers.addAll(cacheManagers);
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
}
