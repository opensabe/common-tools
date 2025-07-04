package io.github.opensabe.common.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.common.cache.api.CompositeCacheManager;
import io.github.opensabe.common.cache.caffeine.DynamicCaffeineCacheManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 根据<code>CachesProperties</code>,创建cacheManager添加到
 * <code>CompositedCacheManager</code>中
 * @see CachesProperties
 * @author heng.ma
 */
@ConditionalOnClass(Caffeine.class)
public class CaffeineConfiguration implements InitializingBean {

    private final CachesProperties cachesProperties;
    private final CacheManagerCustomizers customizers;
    private final CompositeCacheManager compositeCacheManager;

    public CaffeineConfiguration(CachesProperties cachesProperties, CacheManagerCustomizers customizers, CompositeCacheManager compositeCacheManager) {
        this.cachesProperties = cachesProperties;
        this.customizers = customizers;
        this.compositeCacheManager = compositeCacheManager;
    }


    @Override
    public void afterPropertiesSet() {
        List<CacheManager> cacheManagers = new ArrayList<>();
        cacheManagers.add(dynamicCaffeineCacheManager());
        if (cachesProperties.isEnabled() && cachesProperties.getCustom()!= null) {
            List<CacheManager> list = cachesProperties.getCustom().stream().filter(p -> CacheType.CAFFEINE.equals(p.getType()))
                    .map(this::caffeineCacheManager)
                    .toList();
            if (!list.isEmpty()) {
                cacheManagers.addAll(list);
            }
        }
        compositeCacheManager.setCacheManagers(cacheManagers);
    }

    public DynamicCaffeineCacheManager dynamicCaffeineCacheManager () {
        DynamicCaffeineCacheManager cacheManager = new DynamicCaffeineCacheManager(cachesProperties);
        return customizers.customize(cacheManager);
    }

    private CacheManager caffeineCacheManager (CacheProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        if (properties != null) {
            CacheProperties.Caffeine caffeine = properties.getCaffeine();
            if (StringUtils.hasText(caffeine.getSpec())) {
                Arrays.stream(caffeine.getSpec().split(","))
                        .filter(op -> op.contains("allowNullValues"))
                        .findFirst().ifPresent(op -> cacheManager.setAllowNullValues(Boolean.parseBoolean(op.split("=")[1].trim())));
                cacheManager.setCacheSpecification(caffeine.getSpec());
            }
            if (!properties.getCacheNames().isEmpty()) {
                cacheManager.setCacheNames(properties.getCacheNames());
            }
        }
        return customizers.customize(cacheManager);
    }

}
