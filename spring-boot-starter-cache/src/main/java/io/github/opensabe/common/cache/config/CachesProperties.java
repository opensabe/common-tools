package io.github.opensabe.common.cache.config;

import io.github.opensabe.common.cache.utils.CacheHelper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@ConfigurationProperties(prefix = "caches")
public class CachesProperties {

    private List<CustomCacheProperties> custom;
    private boolean enabled = true;
    @PostConstruct
    public void initCacheNamesPrefix(){
        if(!CollectionUtils.isEmpty(custom)){
            custom.forEach(customList -> customList.setCacheNames(
                    customList.getCacheNames().stream()
                    .map(cacheName -> CacheHelper.CACHE_NAME_PREFIX + cacheName)
                    .collect(Collectors.toList())
            ));
        }
    }

    public static class CustomCacheProperties extends CacheProperties{

        private String cacheDesc;

        public String getCacheDesc() {
            return this.cacheDesc;
        }

        public void setCacheDesc(String cacheDesc) {
            this.cacheDesc = cacheDesc;
        }

        public Object getCacheSetting(){
            switch (getType()) {
                case CAFFEINE:
                    return getCaffeine();
                case COUCHBASE:
                    return getCouchbase();
//                case EHCACHE:
//                    return getEhcache();
                case INFINISPAN:
                    return getInfinispan();
                case JCACHE:
                    return getJcache();
                case REDIS:
                    return getRedis();
                default:
                    return null;
            }
        }
    }
}
