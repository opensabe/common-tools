package io.github.opensabe.common.cache.cache;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import io.github.opensabe.common.cache.utils.CacheHelper;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.LinkedHashSet;
import java.util.List;

@AllArgsConstructor
public class RedisCustomCache implements CustomCache {

    private final CacheManagerCustomizers customizers;
    private final ObjectProvider<RedisCacheConfiguration> redisCacheConfiguration;
    private final ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public CacheManager cacheManager(CacheProperties cacheProperties) {
        return cacheManager(cacheProperties, this.customizers, this.redisCacheConfiguration, this.redisCacheManagerBuilderCustomizers, this.redisConnectionFactory);
    }

    public RedisCacheManager cacheManager(CacheProperties cacheProperties, CacheManagerCustomizers customizers, ObjectProvider<RedisCacheConfiguration> redisCacheConfiguration, ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers, RedisConnectionFactory redisConnectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(determineConfiguration(cacheProperties, redisCacheConfiguration));
        List<String> cacheNames = cacheProperties.getCacheNames();
        if (!cacheNames.isEmpty()) {
            builder.initialCacheNames(new LinkedHashSet<>(cacheNames));
        }

        if (cacheProperties.getRedis().isEnableStatistics()) {
            builder.enableStatistics();
        }

        redisCacheManagerBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));

        RedisCacheManager cacheManager = builder.build();
        var cacheWriter = CacheHelper.readFiled("cacheWriter",cacheManager,RedisCacheWriter.class);
        var defaultConfiguration = CacheHelper.readFiled("defaultCacheConfiguration",cacheManager,RedisCacheConfiguration.class);

        CustomRedisCacheManager customRedisCacheManager = new CustomRedisCacheManager(cacheWriter, defaultConfiguration, cacheProperties);
        BeanUtils.copyProperties(cacheManager, customRedisCacheManager);

        return customizers.customize(customRedisCacheManager);
    }

    private RedisCacheConfiguration determineConfiguration(CacheProperties cacheProperties, ObjectProvider<RedisCacheConfiguration> redisCacheConfiguration) {
        return redisCacheConfiguration.getIfAvailable(() -> createConfiguration(cacheProperties));
    }

    private RedisCacheConfiguration createConfiguration(CacheProperties cacheProperties) {
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }

        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }

        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        return config;
    }
    public static class CustomRedisCacheManager extends RedisCacheManager {

        private final CacheProperties cacheProperties;

        public CustomRedisCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration, CacheProperties cacheProperties) {
            super(cacheWriter, defaultCacheConfiguration);
            this.cacheProperties = cacheProperties;
        }

        //Override RedisCacheManager getMissingCache策略
        //只创建YAML上存在的Cache Name
        @Override
        protected RedisCache getMissingCache(String name) {
            return cacheProperties.getCacheNames().contains(name)? super.getMissingCache(name):null;
        }
    }
}
