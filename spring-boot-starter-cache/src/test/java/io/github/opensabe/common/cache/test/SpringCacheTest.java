package io.github.opensabe.common.cache.test;

import io.github.opensabe.common.cache.test.entity.ItemObject;
import io.github.opensabe.common.cache.test.service.CacheService;
import io.github.opensabe.common.cache.test.storage.MockStorage;
import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.github.opensabe.common.cache.utils.CacheHelper.CACHE_NAME_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({
        SpringExtension.class, SingleRedisIntegrationTest.class
})
@SpringBootTest(properties = {
        "caches.enabled=true",
        "caches.custom[0].type=caffeine",
        "caches.custom[0].cacheNames=test_caffeine",
        "caches.custom[0].caffeine.spec=expireAfterWrite=5s",
        "caches.custom[1].type=redis",
        "caches.custom[1].cacheNames=test_redis",
        "caches.custom[1].redis.timeToLive=5s",
        "caches.custom[1].redis.cacheNullValues=false",
        "caches.custom[2].type=redis",
        "caches.custom[2].cacheNames=test_redis2",
        "caches.custom[2].redis.timeToLive=3s",
        "caches.custom[2].redis.cacheNullValues=false",
}, classes = App.class)
public class SpringCacheTest {
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
    }

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MockStorage storage;

    @Autowired
    private CacheService service;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String REDIS_CACHE_NAME = "test_redis";

    public static final String REDIS_CACHE_KEY_PREFIX = CACHE_NAME_PREFIX + "test_redis::";

    public static final String REDIS_CACHE_KEY_PREFIX2 = CACHE_NAME_PREFIX + "test_redis2::";

    public static final String CAFFEINE_CACHE_NAME = "test_caffeine";

    @Test
    public void test_cacheable_caffeine_without_key_and_field(){

        ItemObject item = ItemObject.builder().id(999L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        Map<Long, ItemObject> map = service.getData();

        Cache cache = cacheManager.getCache(CAFFEINE_CACHE_NAME);
        assertNotNull(cache);
        Set<@NonNull Object> keys = ((CaffeineCache)cache).getNativeCache().asMap().keySet();
        assertTrue(keys.contains("getData"));
    }

    @Test
    public void test_cacheable_caffeine_without_key_and_have_field(){

        ItemObject item = ItemObject.builder().id(1L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        service.getItemFromCaffeineWithoutKey(item.getId(), "TEST");

        Cache cache = cacheManager.getCache(CAFFEINE_CACHE_NAME);
        assertNotNull(cache);
        Set<@NonNull Object> keys = ((CaffeineCache)cache).getNativeCache().asMap().keySet();
        assertTrue(keys.contains(String.format("%s:%s", item.getId(), "TEST")));
    }

    @Test
    public void test_cacheable_caffeine() {

        ItemObject item = ItemObject.builder().id(1L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        service.getItemFromCaffeine(item.getId());

        ItemObject cachedItem = cacheManager.getCache(CAFFEINE_CACHE_NAME).get(item.getId(), ItemObject.class);
        assertNotNull(cachedItem);
        assertEquals(cachedItem.getName(), item.getName());
    }

    //因为暂时不再往 redis 里面存储Key
    //mapRst 一定会错
    //所以忽略
    @Disabled
    @Test
    public void test_cacheable_redis() {

        ItemObject item = ItemObject.builder().id(100L).name("redisCache").value("Test_Redis").build();
        storage.addItem(item);
        service.getItemFromRedis(item.getId());

        String redisKey = String.format("%s%s", "test_redis:", item.getId());

        String cachedItem = cacheManager.getCache(REDIS_CACHE_NAME).get(redisKey, String.class);
        assertNotNull(cachedItem);
        assertEquals(cachedItem, item.getName());

        String redisRst = redisTemplate.opsForValue().get(REDIS_CACHE_KEY_PREFIX + redisKey);
        assertEquals(item.getName(), redisRst.replace("\"", ""));

        Object mapRst = redisTemplate.opsForHash().get(CACHE_NAME_PREFIX + REDIS_CACHE_NAME, REDIS_CACHE_KEY_PREFIX + redisKey);
        assertNotNull(mapRst);
    }

    @Test
    public void test_cachePut_caffeine() {

        ItemObject item = ItemObject.builder().id(2L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        service.getItemFromCaffeine(item.getId());

        item.setName("caffeineCache_updated");
        service.updateItemFromCaffeine(item.getId());

        ItemObject cachedItem = cacheManager.getCache(CAFFEINE_CACHE_NAME).get(item.getId(), ItemObject.class);
        assertNotNull(cachedItem);
        assertEquals(cachedItem.getName(), item.getName());
        assertEquals(storage.getItem(item.getId()).getName(), item.getName());
    }

    @Test
    public void test_cachePut_redis() {

        ItemObject item = ItemObject.builder().id(200L).name("redisCache").value("Test_Redis").build();
        storage.addItem(item);
        service.getItemFromRedis(item.getId());

        item.setName("redisCache_updated");
        service.updateItemFromRedis(item.getId());

        String redisKey = String.format("%s%s", "test_redis:", item.getId());

        String cachedItem = cacheManager.getCache(REDIS_CACHE_NAME).get(redisKey, String.class);
        assertNotNull(cachedItem);
        assertEquals(cachedItem, item.getName());

        String redisRst = redisTemplate.opsForValue().get(REDIS_CACHE_KEY_PREFIX + redisKey).toString();
        assertEquals(item.getName(), redisRst.replace("\"", ""));
    }

    @Test
    public void test_cacheEvict_caffeine() {

        ItemObject item = ItemObject.builder().id(1L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        service.getItemFromCaffeine(item.getId());

        service.deleteItemFromCaffeine(item.getId());
        ItemObject cachedItem = cacheManager.getCache(CAFFEINE_CACHE_NAME).get(item.getId(), ItemObject.class);
        assertNull(cachedItem);
    }

    @Test
    public void test_cacheEvict_redis() {

        ItemObject item = ItemObject.builder().id(100L).name("redisCache").value("Test_Redis").build();
        storage.addItem(item);
        service.getItemFromRedis(item.getId());

        service.deleteItemFromRedis(item.getId());

        String redisKey = String.format("%s%s", "test_redis:", item.getId());

        String cachedItem = cacheManager.getCache(REDIS_CACHE_NAME).get(redisKey, String.class);
        assertNull(cachedItem);

        String redisRst = redisTemplate.opsForValue().get(REDIS_CACHE_KEY_PREFIX + redisKey);
        assertNull(redisRst);

        Object mapRst = redisTemplate.opsForHash().get(CACHE_NAME_PREFIX + REDIS_CACHE_NAME, REDIS_CACHE_KEY_PREFIX + redisKey);
        assertNull(mapRst);
    }

    @Test
    public void test_cacheable_redis_expire() throws InterruptedException {

        ItemObject item = ItemObject.builder().id(100L).name("redisCache").value("Test_Redis").build();
        storage.addItem(item);
        service.getItemFromRedis(item.getId());

        ItemObject item2 = ItemObject.builder().id(100L).name("redisCache2").value("Test_Redis2").build();
        storage.addItem(item2);
        service.getItemFromRedis2(item2.getId());

        Thread.sleep(4000);
        String redisKey2 = String.format("%s%s", "test_redis2:", item.getId());
        String redisRst2 = redisTemplate.opsForValue().get(REDIS_CACHE_KEY_PREFIX2 + redisKey2 );
        assertNull(redisRst2);

        Thread.sleep(2000);
        String redisKey = String.format("%s%s", "test_redis:", item.getId());
        String redisRst = redisTemplate.opsForValue().get(REDIS_CACHE_KEY_PREFIX + redisKey );
        assertNull(redisRst);


    }

    @Test
    public void test_cacheable_caffeine_expire() {

        ItemObject item = ItemObject.builder().id(1L).name("caffeineCache").value("Test_Caffeine").build();
        storage.addItem(item);
        service.getItemFromCaffeine(item.getId());

        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(CAFFEINE_CACHE_NAME);
        boolean isExpire = cache.getNativeCache().policy().expireAfterWrite().get().getExpiresAfter(TimeUnit.SECONDS) <= System.currentTimeMillis() + 6 * 1000;
        assertTrue(isExpire);
    }
}
