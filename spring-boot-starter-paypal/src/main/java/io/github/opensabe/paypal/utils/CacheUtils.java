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
package io.github.opensabe.paypal.utils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import static java.util.Objects.isNull;

/**
 * 工具类：基于caffeine，自定义元素过期时间
 *
 * @author guo
 */
public class CacheUtils {
    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = Long.MAX_VALUE;

    public static final Cache<String, CacheObject<?>> CAFFEINE = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, CacheObject<?>>() {
                @Override
                public long expireAfterCreate(String key, CacheObject<?> value, long currentTime) {
                    return value.expire;
                }

                @Override
                public long expireAfterUpdate(String key, CacheObject<?> value, long currentTime, long currentDuration) {
                    return value.expire;
                }

                @Override
                public long expireAfterRead(String key, CacheObject<?> value, long currentTime, long currentDuration) {
                    return value.expire;
                }
            })
            .initialCapacity(100)
            .maximumSize(1024)
            .build();

    public static <T> void set(String key, T value, long expire) {
        CacheObject<T> cacheObject = new CacheObject<>(value, expire);
        CAFFEINE.put(key, cacheObject);
    }

    public static void set(String key, Object value) {
        set(key, value, NOT_EXPIRE);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        CacheObject<?> cacheObject = CAFFEINE.getIfPresent(key);
        if (isNull(cacheObject)) {
            return null;
        }
        return (T) cacheObject.data;
    }

    public static void delete(String key) {
        CAFFEINE.invalidate(key);
    }

    public static void delete(Collection<String> keys) {
        CAFFEINE.invalidateAll(keys);
    }

    public static void main(String[] args) throws InterruptedException {
        CacheUtils.set("A", 1, 2);
        CacheUtils.set("B", 2, 5);
        CacheUtils.set("C", 3, 8);

        System.out.println(CAFFEINE.asMap());

        Object a = CacheUtils.get("A");
        Object b = CacheUtils.get("B");
        Object c = CacheUtils.get("C");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println("-----------------");
        Thread.sleep(2000);

        a = CacheUtils.get("A");
        b = CacheUtils.get("B");
        c = CacheUtils.get("C");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println("-----------------");
        Thread.sleep(5000);
        a = CacheUtils.get("A");
        b = CacheUtils.get("B");
        c = CacheUtils.get("C");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println("-----------------");
        Thread.sleep(8000);
        a = CacheUtils.get("A");
        b = CacheUtils.get("B");
        c = CacheUtils.get("C");
        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println("-----------------");
    }

    private static class CacheObject<T> {
        T data;
        long expire;

        public CacheObject(T data, long second) {
            this.data = data;
            this.expire = TimeUnit.SECONDS.toNanos(second);
        }
    }

}