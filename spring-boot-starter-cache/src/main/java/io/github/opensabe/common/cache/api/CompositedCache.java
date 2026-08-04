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
package io.github.opensabe.common.cache.api;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.springframework.cache.Cache;

/**
 * 聚合多个底层 {@link Cache} 的只读/驱逐门面，用于 {@code @CacheEvict} 未指定 {@link Expire} 时。
 * <p>
 * 此时 {@link org.springframework.cache.CacheManager#getCache(String)} 只会按 cache 名称查找，
 * 不会携带 TTL，因此需返回已创建的各 TTL 变体集合，以便 evict/clear 能覆盖全部条目。
 * </p>
 * <p>
 * {@link ExpireCacheManager} 动态创建缓存；evict 仅需操作已存在的实例，故 {@code getCache(name)}
 * 仅聚合当前已创建的 cache，而非预创建全部 TTL 组合。
 * </p>
 *
 * @author heng.ma
 * @see org.springframework.cache.CacheManager#getCache(String)
 */
public class CompositedCache implements Cache {

    /**
     * 逻辑缓存名称。
     */
    private final String name;

    /**
     * 同一 cache 名称下已创建的底层缓存实例（含不同 TTL 或专用 manager 实例）。
     */
    private final Collection<Cache> list;

    /**
     * @param name 缓存名称
     * @param list 待聚合的底层缓存实例
     */
    public CompositedCache(String name, Collection<Cache> list) {
        this.name = name;
        this.list = list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * 不支持暴露原生缓存对象。
     *
     * @throws UnsupportedOperationException 始终抛出
     */
    @Override
    public Object getNativeCache() {
        throw new UnsupportedOperationException();
    }

    /**
     * 按注册顺序在底层 cache 中查找第一个命中的值。
     *
     * @param key 缓存键
     * @return 值包装，均未命中时 {@code null}
     */
    @Override
    public ValueWrapper get(Object key) {
        for (Cache cache : list) {
            ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                return wrapper;
            }
        }
        return null;
    }

    /**
     * 按注册顺序在底层 cache 中查找第一个非 {@code null} 的强类型值。
     * <p>
     * 将各底层 cache 返回的 cached-null 包装视为未命中（与 {@code cacheNullValues=false} 语义一致）。
     * </p>
     *
     * @param key  缓存键
     * @param type 期望类型
     * @param <T>  值类型
     * @return 缓存值，均未命中时 {@code null}
     */
    @Override
    public <T> T get(Object key, Class<T> type) {
        for (Cache cache : list) {
            T value = cache.get(key, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * 若任一底层 cache 命中则返回值，否则调用 {@code valueLoader} 加载并写入全部底层 cache。
     *
     * @param key         缓存键
     * @param valueLoader 加载回调
     * @param <T>         值类型
     * @return 缓存或加载得到的值
     * @throws ValueRetrievalException 加载失败时
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            @SuppressWarnings("unchecked")
            T value = (T) wrapper.get();
            return value;
        }
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
    }

    /**
     * 将键值写入全部底层 cache。
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    @Override
    public void put(Object key, Object value) {
        list.forEach(cache -> cache.put(key, value));
    }

    /**
     * 从全部底层 cache 驱逐指定键。
     *
     * @param key 缓存键
     */
    @Override
    public void evict(Object key) {
        list.forEach(cache -> cache.evict(key));
    }

    /**
     * 清空全部底层 cache。
     */
    @Override
    public void clear() {
        list.forEach(Cache::clear);
    }
}
