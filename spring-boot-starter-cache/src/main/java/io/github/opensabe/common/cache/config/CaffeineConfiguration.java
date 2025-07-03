package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.caffeine.DynamicCaffeineCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author heng.ma
 */
public class CaffeineConfiguration {

    private final CacheProperties properties;

    public CaffeineConfiguration(@Autowired(required = false) CachesProperties cacheProperties) {
        this.properties = Optional.ofNullable(cacheProperties)
                .filter(CachesProperties::isEnabled)
                .map(CachesProperties::getCustom)
                .stream().flatMap(List::stream)
                .filter(p -> CacheType.CAFFEINE.equals(p.getType()))
                .findFirst()
                .orElse(null);
    }

    @Bean
    public DynamicCaffeineCacheManager dynamicCaffeineCacheManager (CacheManagerCustomizers customizers) {
        CacheProperties.Caffeine caffeine = properties == null? null :properties.getCaffeine();
        DynamicCaffeineCacheManager cacheManager = new DynamicCaffeineCacheManager(caffeine == null ? "" : caffeine.getSpec());
        return customizers.customize(cacheManager);
    }

    @Bean
    public CaffeineCacheManager caffeineCacheManager (DynamicCaffeineCacheManager dynamicCaffeineCacheManager, CacheManagerCustomizers customizers) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        if (properties != null) {
            CacheProperties.Caffeine caffeine = properties.getCaffeine();
            if (StringUtils.hasText(caffeine.getSpec())) {
                cacheManager.setCacheSpecification(caffeine.getSpec());
            }
            if (!properties.getCacheNames().isEmpty()) {
                cacheManager.setCacheNames(properties.getCacheNames());
            }
        }
        cacheManager.setAllowNullValues(dynamicCaffeineCacheManager.isAllowNullValues());
        return customizers.customize(cacheManager);
    }
}
