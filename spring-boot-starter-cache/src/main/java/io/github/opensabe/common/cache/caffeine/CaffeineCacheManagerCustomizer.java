package io.github.opensabe.common.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.caffeine.CaffeineCache;

/**
 * @author heng.ma
 */
public interface CaffeineCacheManagerCustomizer extends CacheManagerCustomizer<DynamicCaffeineCacheManager> {
    @Override
    default void customize(DynamicCaffeineCacheManager cacheManager) {
        cacheManager.onCaffeine((name, caffeine) -> createCache(name, caffeine, cacheManager.isAllowNullValues()));
    }

    /**
     * 自定义创建Cache实例，可以添加监听器，设置引用类型等
     * @param name      cacheName
     * @param caffeine  CacheBuilder
     * @return          spring cache instance of CaffeineCache
     */
    CaffeineCache createCache(String name, Caffeine<Object, Object> caffeine, boolean allowNullValues);
}
