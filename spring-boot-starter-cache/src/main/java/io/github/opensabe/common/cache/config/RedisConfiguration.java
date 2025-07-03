package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.redis.DynamicRedisCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * @author heng.ma
 */
public class RedisConfiguration {

    private final CacheProperties.Redis properties;

    public RedisConfiguration(@Autowired(required = false) CacheProperties properties) {
        this.properties = properties != null ? properties.getRedis() : null;
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRedisCacheManager dynamicRedisCacheManager (RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                //为了防止意外，加个默认的过期时间
                .entryTtl(Duration.ofDays(7));
        if (properties != null) {
            String keyPrefix = properties.getKeyPrefix();
            if (StringUtils.hasText(keyPrefix)) {
                configuration = configuration.prefixCacheNameWith(keyPrefix);
            }
            if (!properties.isCacheNullValues()) {
                configuration = configuration.disableCachingNullValues();
            }
            if (!properties.isUseKeyPrefix()) {
                configuration = configuration.disableKeyPrefix();
            }
        }
        return new DynamicRedisCacheManager(redisConnectionFactory, configuration);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisCacheManager redisCacheManager (RedisConnectionFactory redisConnectionFactory) {
        if (properties == null) {
            return null;
        }
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        Duration timeToLive = properties.getTimeToLive();
        if (timeToLive != null) {
            configuration = configuration.entryTtl(timeToLive);
        }
        String keyPrefix = properties.getKeyPrefix();
        if (StringUtils.hasText(keyPrefix)) {
            configuration = configuration.prefixCacheNameWith(keyPrefix);
        }
        if (!properties.isCacheNullValues()) {
            configuration = configuration.disableCachingNullValues();
        }
        if (!properties.isUseKeyPrefix()) {
            configuration = configuration.disableKeyPrefix();
        }
        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(configuration).build();
    }
}
