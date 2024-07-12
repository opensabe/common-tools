package io.github.opensabe.common.cache.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.github.opensabe.common.cache.utils.CacheHelper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@AllArgsConstructor
public class CaffeineCustomCache implements CustomCache {

    private final CacheManagerCustomizers customizers;
    private final ObjectProvider<Caffeine<Object, Object>> caffeine;
    private final ObjectProvider<CaffeineSpec> caffeineSpec;
    private final ObjectProvider<List<CacheLoader<Object, Object>>> cacheLoader;
    private final ApplicationContext context;

    @Override
    public CacheManager cacheManager(CacheProperties cacheProperties) {
        return cacheManager(cacheProperties, this.customizers, this.caffeine, this.caffeineSpec, this.cacheLoader);
    }

    public CaffeineCacheManager cacheManager(CacheProperties cacheProperties, CacheManagerCustomizers customizers, ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec, ObjectProvider<List<CacheLoader<Object, Object>>> cacheLoader) {
        CaffeineCacheManager cacheManager = createCacheManager(cacheProperties, caffeine, caffeineSpec, cacheLoader);
        List<String> cacheNames = cacheProperties.getCacheNames();
        if (!CollectionUtils.isEmpty(cacheNames)) {
            cacheManager.setCacheNames(cacheNames);
        }

        return customizers.customize(cacheManager);
    }

    private CaffeineCacheManager createCacheManager(CacheProperties cacheProperties, ObjectProvider<Caffeine<Object, Object>> caffeine, ObjectProvider<CaffeineSpec> caffeineSpec, ObjectProvider<List<CacheLoader<Object, Object>>> cacheLoader) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        setCacheBuilder(cacheProperties, caffeineSpec.getIfAvailable(), caffeine.getIfAvailable(), cacheManager);
        cacheLoader.ifAvailable( cl -> {
            try {
                var loader = context.getBean(cacheProperties.getCacheNames().get(0).replace(CacheHelper.CACHE_NAME_PREFIX, ""), CacheLoader.class);
                cacheManager.setCacheLoader(loader);
            } catch (Throwable e) {
                log.fatal("CaffeineCacheManager cacheLoader init failed, please check your configuration", e);
            }
        });
        return cacheManager;
    }

    private void setCacheBuilder(CacheProperties cacheProperties, CaffeineSpec caffeineSpec, Caffeine<Object, Object> caffeine, CaffeineCacheManager cacheManager) {
        String specification = cacheProperties.getCaffeine().getSpec();
        if (StringUtils.hasText(specification)) {
            if (specification.contains("cacheNullValues")){
                specification = Arrays.stream(specification.split(",")).
                        filter(s -> {
                            if (s.contains("cacheNullValues")){
                                boolean allowNullValues = s.split("=")[1].trim().equals("true");
                                cacheManager.setAllowNullValues(allowNullValues);
                                return false;
                            }else{
                                return true;
                            }
                        }).collect(Collectors.joining(","));
            }
            cacheManager.setCacheSpecification(specification);
        } else if (caffeineSpec != null) {
            cacheManager.setCaffeineSpec(caffeineSpec);
        } else if (caffeine != null) {
            cacheManager.setCaffeine(caffeine);
        }
    }
}
