package io.github.opensabe.common.cache.config;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.github.opensabe.common.cache.actuator.CacheEndpoint;
import io.github.opensabe.common.cache.cache.CaffeineCustomCache;
import io.github.opensabe.common.cache.cache.CustomCache;
import io.github.opensabe.common.cache.cache.CustomCompositeCacheManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value = {CachesProperties.class})
public class CustomCacheConfiguration {
    @Bean
    @Primary
    public CompositeCacheManager compositeCacheManager(CachesProperties cachesProperties, ApplicationContext context) {
        List<CacheManager> cacheManagers = new ArrayList<>(cachesProperties.getCustom().size());
        cachesProperties.getCustom().forEach(cache -> {
            CacheManager cacheManager;
            try {
                CustomCache manager = context.getBean(cache.getType().name().toLowerCase(), CustomCache.class);
                cacheManager = manager.cacheManager(cache);
            }catch (Throwable e){
                throw new IllegalArgumentException("Unsupported cache type: " + cache.getType(), e);
            }
            cacheManagers.add(cacheManager);
        });
        return new CustomCompositeCacheManager(cacheManagers.toArray(new CacheManager[cachesProperties.getCustom().size()]));
    }

    @Bean("caffeine")
    public CaffeineCustomCache caffeineCustomCache(CacheManagerCustomizers customizers, ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec, ObjectProvider<List<CacheLoader<Object, Object>>> cacheLoader, ApplicationContext context){
        return new CaffeineCustomCache(customizers, caffeine, caffeineSpec, cacheLoader, context);
    }

    @Bean
    public CacheEndpoint cacheEndpoint(CompositeCacheManager manager, ApplicationContext context, CachesProperties cachesProperties){
        return new CacheEndpoint(manager, context, cachesProperties);
    }
}
