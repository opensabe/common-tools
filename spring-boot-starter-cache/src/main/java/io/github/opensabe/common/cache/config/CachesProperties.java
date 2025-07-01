package io.github.opensabe.common.cache.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "caches")
public class CachesProperties {

    private List<CustomCacheProperties> custom;
    private boolean enabled = true;

    @Getter
    @Setter
    public static class CustomCacheProperties extends CacheProperties{

        private String cacheDesc;


        public Object getCacheSetting(){
            return switch (getType()) {
                case CAFFEINE -> getCaffeine();
                case COUCHBASE -> getCouchbase();
//                case EHCACHE:
//                    return getEhcache();
                case INFINISPAN -> getInfinispan();
                case JCACHE -> getJcache();
                case REDIS -> getRedis();
                default -> null;
            };
        }
    }
}
