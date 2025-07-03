package io.github.opensabe.common.cache.redis;

import io.github.opensabe.common.cache.api.ExpireCacheManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author heng.ma
 */
@Log4j2
public class DynamicRedisCacheManager extends RedisCacheManager implements ExpireCacheManager {

    private final Map<String, Map<Duration, Cache>> map;

    private Function<RedisCacheConfiguration, RedisCacheConfiguration> onRedisCacheConfiguration;

    public DynamicRedisCacheManager(RedisConnectionFactory connectionFactory, RedisCacheConfiguration defaultCacheConfiguration) {
        super(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), defaultCacheConfiguration);
        this.map = new ConcurrentHashMap<>();
        this.onRedisCacheConfiguration = Function.identity();
    }


    @Override
    public Cache getCache(String name, Duration ttl) {
        return map.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).computeIfAbsent(ttl, k -> {
            RedisCacheConfiguration configuration = getDefaultCacheConfiguration().entryTtl(ttl);
            configuration = onRedisCacheConfiguration.apply(configuration);
            return super.decorateCache(new RCache(name, getCacheWriter(), configuration));
        });
    }

    public static class RCache extends RedisCache {

        private RCache(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfiguration) {
            super(name, cacheWriter, cacheConfiguration);
        }

        @Override
        protected String createCacheKey(Object key) {
            String cacheKey = super.createCacheKey(key);
            if (log.isDebugEnabled()) {
                log.debug("Spring cache redis key: {}", cacheKey);
            }
            return cacheKey;
        }
    }

    /**
     * 屏蔽普通的cache
     */
    @Override
    public Cache getCache(String name) {
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return map.keySet();
    }

    @Override
    protected Collection<RedisCache> loadCaches() {
        return map.values()
                .stream()
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(c -> isTransactionAware()? ((TransactionAwareCacheDecorator)c).getTargetCache():c)
                .map(c -> (RedisCache)c)
                .toList();
    }



    public DynamicRedisCacheManager onRedisCacheConfiguration(Function<RedisCacheConfiguration, RedisCacheConfiguration> onRedisCacheConfiguration) {
        this.onRedisCacheConfiguration = this.onRedisCacheConfiguration.andThen(onRedisCacheConfiguration);
        return this;
    }
}
