package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.api.CompositeCacheManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;

/**
 * @author heng.ma
 */
public class CacheManagerConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        return new CacheManagerCustomizers(customizers.orderedStream().toList());
    }


    @Bean
    public CompositeCacheManager compositeCacheManager () {
        return new CompositeCacheManager();
    }
}
