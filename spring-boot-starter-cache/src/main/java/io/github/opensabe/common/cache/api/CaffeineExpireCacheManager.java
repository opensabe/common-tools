package io.github.opensabe.common.cache.api;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import jakarta.annotation.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.time.Duration;
import java.util.Collection;

/**
 * @author heng.ma
 */
public class CaffeineExpireCacheManager extends CaffeineCacheManager implements ExpireCacheManager {

    private CaffeineSpec caffeineSpec;
    @Nullable
    @Override
    public Cache getCache(String name, Duration ttl) {

        return adaptCaffeineCache(name, Caffeine.from(caffeineSpec).expireAfterWrite(ttl).build());
    }


    @Override
    public void setCacheNames(Collection<String> cacheNames) {
        super.setCacheNames(cacheNames);

    }

    @Override
    public void setCaffeine(Caffeine<Object, Object> caffeine) {
        super.setCaffeine(caffeine);
    }

    @Override
    public void setCaffeineSpec(CaffeineSpec caffeineSpec) {
        super.setCaffeineSpec(caffeineSpec);
        this.caffeineSpec = caffeineSpec;
    }

    @Override
    public void setCacheSpecification(String cacheSpecification) {
        super.setCacheSpecification(cacheSpecification);
        this.caffeineSpec = CaffeineSpec.parse(cacheSpecification);
    }

}
