/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    /**
     * 如果<code>properties</code>预先定义了cacheName,那么在创建该cache时，
     * 使用预定义的这些settings
     * @param properties 预先定义的cacheName
     */
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
     * 因为caffeine是本地缓存，并且<code>ExpireCacheManager</code>是动态创建缓存的，
     * 不同的key可能会分散到不同的cache上，因此删除时需要遍历所有的cache
     * @see CompositedCache
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
