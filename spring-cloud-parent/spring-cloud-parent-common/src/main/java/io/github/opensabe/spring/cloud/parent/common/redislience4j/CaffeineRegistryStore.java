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
package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.github.resilience4j.core.RegistryStore;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 用于动态替换 resilience4j 的 InMemoryStore 默认的实现，默认实现永远不会过期，导致缓存不断变大
 * @param <E>
 */
@Log4j2
public class CaffeineRegistryStore<E> implements RegistryStore<E> {
    private final Cache<String, E> cache = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, E>) (key, value, cause) -> {
                if (value instanceof AutoCloseable) {
                    AutoCloseable closeable = (AutoCloseable) value;
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        log.error("CaffeineRegistryStore auto-closeable error: {}, {}", key, value, e);
                    }
                    log.info("CaffeineRegistryStore is a auto-closeable and is closed: {}, {}, {}", key, value, cause);
                }
                log.info("CaffeineRegistryStore expired: {}, {}, {}", key, value, cause);
            }).build();

    @Override
    public E computeIfAbsent(String key, Function<? super String, ? extends E> mappingFunction) {
        return cache.get(key, mappingFunction);
    }

    @Override
    public E putIfAbsent(String key, E value) {
        E previous = cache.getIfPresent(key);
        if (previous == null) {
            cache.put(key, value);
        }
        return previous;
    }

    @Override
    public Optional<E> find(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public Optional<E> remove(String name) {
        E previous = cache.getIfPresent(name);
        cache.invalidate(name);
        return Optional.ofNullable(previous);
    }

    @Override
    public Optional<E> replace(String name, E newEntry) {
        E previous = cache.getIfPresent(name);
        cache.put(name, newEntry);
        return Optional.ofNullable(previous);
    }

    @Override
    public Collection<E> values() {
        return cache.asMap().values();
    }
}
