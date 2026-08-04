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
package io.github.opensabe.common.redisson.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.github.opensabe.common.redisson.util.BaseBatchLoadingRedisCache;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * BaseBatchLoadingRedisCache 序列化契约测试：写入走 JsonUtil、读取走 Fastjson，升级后往返须保持稳定。
 */
@Import(BaseBatchLoadingRedisCacheContractTest.Config.class)
@DisplayName("BaseBatchLoadingRedisCache JsonUtil/Fastjson 契约")
class BaseBatchLoadingRedisCacheContractTest extends BaseRedissonTest {

    @Autowired
    private SampleCache sampleCache;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @DisplayName("冷缓存加载后写入 Redis，第二次命中缓存且对象等价")
    void roundTripThroughCache() {
        sampleCache.resetLoadCount();
        sampleCache.seedSource(List.of(
                new CacheItem("a", "name-a", new Nested("n-a")),
                new CacheItem("b", "name-b", new Nested("n-b"))
        ));

        List<CacheItem> first = sampleCache.batchGet(List.of("a", "b"));
        assertEquals(2, first.size());
        assertEquals(1, sampleCache.loadCount());
        assertEquals("name-a", byKey(first, "a").getName());
        assertEquals("n-a", byKey(first, "a").getNested().getLabel());

        String raw = stringRedisTemplate.opsForValue().get(sampleCache.getKey("a"));
        assertEquals(JsonUtil.toJSONString(byKey(first, "a")), raw);

        List<CacheItem> second = sampleCache.batchGet(List.of("a", "b"));
        assertEquals(2, second.size());
        assertEquals(1, sampleCache.loadCount(), "second call must be full cache hit");
        assertEquals(byKey(first, "a").getName(), byKey(second, "a").getName());
        assertEquals(byKey(first, "a").getNested().getLabel(), byKey(second, "a").getNested().getLabel());
        assertEquals(byKey(first, "b").getName(), byKey(second, "b").getName());
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.hasKey(sampleCache.getKey("a"))));
    }

    private static CacheItem byKey(List<CacheItem> items, String key) {
        return items.stream().filter(i -> key.equals(i.getKey())).findFirst().orElseThrow();
    }

    static class Config {
        @Bean
        SampleCache sampleCache() {
            return new SampleCache();
        }
    }

    static class SampleCache extends BaseBatchLoadingRedisCache<CacheItem> {

        private final Map<String, CacheItem> source = new ConcurrentHashMap<>();
        private final AtomicInteger loads = new AtomicInteger();

        void seedSource(Collection<CacheItem> items) {
            source.clear();
            items.forEach(i -> source.put(i.getKey(), i));
        }

        int loadCount() {
            return loads.get();
        }

        void resetLoadCount() {
            loads.set(0);
        }

        @Override
        protected Class<CacheItem> getValueClass() {
            return CacheItem.class;
        }

        @Override
        public String getKey(String key) {
            return "batch-cache-contract:" + key;
        }

        @Override
        protected List<CacheItem> batchLoadValues(Collection<String> keys) {
            loads.incrementAndGet();
            return keys.stream().map(source::get).collect(Collectors.toList());
        }

        @Override
        protected long expireInSeconds() {
            return 60;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Nested {
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheItem implements BaseBatchLoadingRedisCache.Item {
        private String key;
        private String name;
        private Nested nested;
    }
}
