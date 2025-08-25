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
package io.github.opensabe.common.cache.test;

import io.github.opensabe.common.cache.api.Expire;
import io.github.opensabe.common.cache.api.ExpireCacheManager;
import io.github.opensabe.common.cache.config.RedisConfiguration;
import io.github.opensabe.common.cache.test.entity.ItemObject;
import io.github.opensabe.common.cache.test.service.CacheService;
import io.github.opensabe.common.cache.test.storage.MockStorage;
import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

/**
 * @author heng.ma
 */
@ExtendWith({
        SpringExtension.class, SingleRedisIntegrationTest.class
})
@SpringBootTest(properties = {
        "caches.enabled=true",
        "caches.custom[0].type=caffeine",
        "caches.custom[0].cacheNames[0]=test_caffeine",
        "caches.custom[0].caffeine.spec=expireAfterWrite=5s",
        "caches.custom[1].type=redis",
        "caches.custom[1].cacheNames[0]=test_redis",
        "caches.custom[1].redis.timeToLive=5s",
        "caches.custom[1].redis.cacheNullValues=false"
}, classes = App.class)
public class ExpireTest {

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
    }

    private final CacheService cacheService;
    private final ExpireCacheManager cacheManager;
    private final StringRedisTemplate redisTemplate;
    private final MockStorage storage;
    @Autowired
    public ExpireTest(CacheService cacheService, ExpireCacheManager cacheManager, StringRedisTemplate redisTemplate, MockStorage storage) {
        this.cacheService = cacheService;
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.storage = storage;
    }

    @Test
    void testCaffeine () throws InterruptedException, NoSuchMethodException {
        ItemObject item = ItemObject.builder().id(1L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        Expire expire = CacheService.class.getMethod("getCaffeineExpire", Long.class, String.class).getAnnotation(Expire.class);
        ItemObject current = cacheService.getCaffeineExpire(1L, "id1");
        Cache cache = cacheManager.getCache("test_caffeine", Duration.of(expire.value(), expire.timeUnit().toChronoUnit()));

        Assertions.assertInstanceOf(CaffeineCache.class, cache);

        ItemObject cached = cache.get("1:id1", ItemObject.class);
        Assertions.assertEquals(current, cached);
        expire.timeUnit().sleep(expire.value());
        Assertions.assertNull(cache.get("1:id1"));
    }
    @Test
    void testRedis () throws NoSuchMethodException {
        ItemObject item = ItemObject.builder().id(2L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        Expire expire = CacheService.class.getMethod("getRedisExpire", Long.class, String.class).getAnnotation(Expire.class);
        long start = System.currentTimeMillis();
        cacheService.getRedisExpire(2L, "id2");
        Cache cache = cacheManager.getCache("test_redis", Duration.of(expire.value(), expire.timeUnit().toChronoUnit()));

        Assertions.assertInstanceOf(RedisCache.class, cache);

        long ttl = redisTemplate.getExpire(RedisConfiguration.DEFAULT_REDIS_KEY_PREFIX+"test_redis::2:id2");
        long end = System.currentTimeMillis();

        System.out.println(ttl);

        Assertions.assertTrue(ttl + Duration.ofMillis(end-start).get(expire.timeUnit().toChronoUnit()) == expire.value());
    }

    @Test
    void testAssignment () throws NoSuchMethodException, InterruptedException {
        ItemObject item = ItemObject.builder().id(2L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        Expire expire = CacheService.class.getMethod("geAssignmentExpire", Long.class, String.class).getAnnotation(Expire.class);
        cacheService.geAssignmentExpire(2L, "id2");
        Cache cache = cacheManager.getCache("test_redis", Duration.of(expire.value(), expire.timeUnit().toChronoUnit()));

        Assertions.assertInstanceOf(CaffeineCache.class, cache);
        expire.timeUnit().sleep(expire.value());
        Assertions.assertNull(cache.get("2:id2"));
    }

    @Test
    void testRemoveCaffeine () throws NoSuchMethodException {
        Long id = 3L;
        String filed = "id3";
        ItemObject item = ItemObject.builder().id(id).name(filed).value("Test_Caffeine").build();
        storage.addItem(item);
        Expire expire = CacheService.class.getMethod("getCaffeineExpire", Long.class, String.class).getAnnotation(Expire.class);
        ItemObject current = cacheService.getCaffeineExpire(id, filed);
        Cache cache = cacheManager.getCache("test_caffeine", Duration.of(expire.value(), expire.timeUnit().toChronoUnit()));

        Assertions.assertInstanceOf(CaffeineCache.class, cache);

        ItemObject cached = cache.get(id+":"+filed, ItemObject.class);
        Assertions.assertEquals(current, cached);
        cacheService.deleteCaffeine(id, filed);
        Assertions.assertNull(cache.get(id+":"+filed));
    }
    @Test
    void testRemoveRedis () throws NoSuchMethodException {
        Long id = 4L;
        String filed = "id4";
        ItemObject item = ItemObject.builder().id(id).name(filed).value("Test_Caffeine").build();
        storage.addItem(item);
        cacheService.getRedisExpire(id, filed);
        cacheService.deleteRedis(id, filed);
        String s = redisTemplate.opsForValue().get(RedisConfiguration.DEFAULT_REDIS_KEY_PREFIX  + "test_redis::" + id + ":" + filed);
        Assertions.assertNull(s);
    }
}
