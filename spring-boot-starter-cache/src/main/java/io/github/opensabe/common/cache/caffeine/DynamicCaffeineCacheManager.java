package io.github.opensabe.common.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.github.opensabe.common.cache.api.CompositedCache;
import io.github.opensabe.common.cache.api.ExpireCacheManager;
import io.github.opensabe.common.cache.config.CachesProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author heng.ma
 */
public class DynamicCaffeineCacheManager implements ExpireCacheManager {

    private final Map<String,CaffeineSpec> caffeineSpec;

    private final Map<String, Map<Duration, Cache>> map;

    private boolean allowNullValues = true;

    private BiFunction<String, Caffeine<Object, Object>, CaffeineCache> adapter = (name, caffeine) -> new CaffeineCache(name, caffeine.build(), isAllowNullValues());

    public DynamicCaffeineCacheManager(CachesProperties properties) {
        this.map = new ConcurrentHashMap<>();
        this.caffeineSpec = new ConcurrentHashMap<>();
        Optional.ofNullable(properties.getCustom()).stream().flatMap(List::stream)
                .filter(p -> !p.getCacheNames().isEmpty())
                .filter(p -> CacheType.CAFFEINE.equals(p.getType()))
                .forEach(p -> p.getCacheNames().forEach(n -> caffeineSpec.putIfAbsent(n, CaffeineSpec.parse(resolveCaffeineSpec(p.getCaffeine().getSpec())))));

    }


    private String resolveCaffeineSpec(String caffeineSpec) {
        return Arrays.stream(caffeineSpec.split(","))
                .filter(op -> !op.contains("expireAfterWrite"))
                .peek(op -> {
                    if (op.contains("allowNullValues")) {
                        setAllowNullValues(Boolean.parseBoolean(op.split("=")[1].trim()));
                    }
                }).collect(Collectors.joining(","));
    }


    @Override
    public Cache getCache(String name, Duration ttl) {
        CaffeineSpec spec = caffeineSpec.get(name);

        return map.computeIfAbsent(name, k -> new ConcurrentHashMap<>()).computeIfAbsent(ttl,
                k -> adapter.apply(name, (spec == null ? Caffeine.newBuilder() :Caffeine.from(spec)).expireAfterWrite(ttl)));
    }

    @Override
    public Collection<String> settings(String name) {
        //TODO 后续完善
        return List.of();
    }


    /**
     * 删除缓存使用，因为清除缓存时可能不设置ttl,因此这里需要返回当前所有的缓存，删除时将所有缓存的key都清楚掉
     */
    @Override
    public Cache getCache(String name) {

        Map<Duration, Cache> caches = map.get(name);
        if (caches != null) {
            return new CompositedCache(name, caches.values());
        }
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        Set<String> set = new HashSet<>();
        set.addAll(caffeineSpec.keySet());
        set.addAll(map.keySet());
        return set;
    }

    public boolean isAllowNullValues() {
        return allowNullValues;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public void onCaffeine(BiFunction<String, Caffeine<Object, Object>, CaffeineCache> adapter) {
        this.adapter = adapter;
    }
}
