package io.github.opensabe.common.cache.config;

import com.google.common.collect.Sets;
import io.github.opensabe.common.cache.api.CompositeCacheManager;
import io.github.opensabe.common.cache.redis.DynamicRedisCacheManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据<code>CachesProperties</code>,创建cacheManager添加到
 * <code>CompositedCacheManager</code>中
 * @see CachesProperties
 * @author heng.ma
 */
public class RedisConfiguration implements InitializingBean {

    public static final String DEFAULT_REDIS_KEY_PREFIX = "sfccmr:";

    private final CacheManagerCustomizers customizers;
    private final RedisConnectionFactory connectionFactory;
    private final CachesProperties properties;
    private final CompositeCacheManager compositeCacheManager;
    public RedisConfiguration(CacheManagerCustomizers customizers, RedisConnectionFactory connectionFactory, CachesProperties properties, CompositeCacheManager compositeCacheManager) {
        this.customizers = customizers;
        this.connectionFactory = connectionFactory;
        this.properties = properties;
        this.compositeCacheManager = compositeCacheManager;
    }

    @Override
    public void afterPropertiesSet() {
        List<CacheManager> cacheManagers = new ArrayList<>();
        cacheManagers.add(dynamicRedisCacheManager());
        if (properties.isEnabled() && properties.getCustom() != null) {
            List<CacheManager> list = properties.getCustom().stream().filter(p -> CacheType.REDIS.equals(p.getType()))
                    .map(p -> redisCacheManager(connectionFactory, customizers, p))
                    .toList();
            if (!list.isEmpty()) {
                cacheManagers.addAll(list);
            }
        }
        compositeCacheManager.setCacheManagers(cacheManagers);
    }

    private RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .prefixCacheNameWith(DEFAULT_REDIS_KEY_PREFIX);
    }

    private DynamicRedisCacheManager dynamicRedisCacheManager () {
        Map<String, RedisCacheConfiguration> map = new HashMap<>();
        List<CachesProperties.CustomCacheProperties> list = properties.getCustom();
        if (list != null) {
            list.stream().filter(p -> CacheType.REDIS.equals(p.getType()))
                    .forEach(p -> p.getCacheNames().forEach(n -> map.put(n, configuration(p.getRedis()))));
        }
        return customizers.customize(new DynamicRedisCacheManager(connectionFactory, defaultCacheConfig(), map));
    }



    private RedisCacheConfiguration configuration (CacheProperties.Redis redis) {
        Duration timeToLive = redis.getTimeToLive();
        RedisCacheConfiguration configuration = defaultCacheConfig();
        if (timeToLive != null) {
            configuration = configuration.entryTtl(timeToLive);
        }
        String keyPrefix = redis.getKeyPrefix();
        if (StringUtils.hasText(keyPrefix)) {
            configuration = configuration.prefixCacheNameWith(keyPrefix);
        }
        if (!redis.isCacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }
        if (!redis.isUseKeyPrefix()) {
            configuration = configuration.disableKeyPrefix();
        }
        return configuration;
    }

    private CacheManager redisCacheManager (RedisConnectionFactory redisConnectionFactory,CacheManagerCustomizers customizers, CacheProperties properties) {
        CacheProperties.Redis redis = properties.getRedis();
        RedisCacheConfiguration configuration = configuration(redis);
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(configuration)
                .disableCreateOnMissingCache();

        if (!properties.getCacheNames().isEmpty()) {
            builder = builder.initialCacheNames(Sets.newHashSet(properties.getCacheNames()));
        }
        RedisCacheManager customize = customizers.customize(builder.build());
        customize.afterPropertiesSet();
        return customize;
    }
}
