package io.github.opensabe.common.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.github.opensabe.common.cache.api.ExpireCacheManager;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author heng.ma
 */
public class DynamicCaffeineCacheManager implements ExpireCacheManager {

    private final CaffeineSpec caffeineSpec;

    private final Map<String, Map<Duration, Cache>> map;

    private boolean allowNullValues = true;

    private BiFunction<String, Caffeine<Object, Object>, CaffeineCache> adapter = (name, caffeine) -> new CaffeineCache(name, caffeine.build(), isAllowNullValues());

    public DynamicCaffeineCacheManager(String caffeineSpec) {
        this.map = new ConcurrentHashMap<>();
        caffeineSpec = Arrays.stream(caffeineSpec.split(","))
                .filter(op -> !op.contains("expireAfterWrite"))
                .peek(op -> {
                    if (op.contains("allowNullValues")) {
                        setAllowNullValues(Boolean.parseBoolean(op.split("=")[1].trim()));
                    }
                }).collect(Collectors.joining(","));
        this.caffeineSpec = CaffeineSpec.parse(caffeineSpec);
    }

    public DynamicCaffeineCacheManager(CacheProperties.Caffeine caffeine) {
        this(caffeine.getSpec());
    }

    @Override
    public Cache getCache(String name, Duration ttl) {
        return map.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).computeIfAbsent(ttl,
                k -> adapter.apply(name, Caffeine.from(caffeineSpec).expireAfterWrite(ttl)));
    }

    @Override
    public Collection<String> getCacheNames() {
        return map.keySet();
    }

    public boolean isAllowNullValues() {
        return allowNullValues;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public DynamicCaffeineCacheManager onCaffeine(BiFunction<String, Caffeine<Object, Object>, CaffeineCache> adapter) {
        this.adapter = adapter;
        return this;
    }
}
