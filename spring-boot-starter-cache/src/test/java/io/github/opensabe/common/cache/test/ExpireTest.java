package io.github.opensabe.common.cache.test;

import io.github.opensabe.common.cache.api.Expire;
import io.github.opensabe.common.cache.api.ExpireCacheManager;
import io.github.opensabe.common.cache.test.entity.ItemObject;
import io.github.opensabe.common.cache.test.service.CacheService;
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
    @Autowired
    public ExpireTest(CacheService cacheService, ExpireCacheManager cacheManager, StringRedisTemplate redisTemplate) {
        this.cacheService = cacheService;
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    @Test
    void testCaffeine () throws InterruptedException, NoSuchMethodException {
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
        Expire expire = CacheService.class.getMethod("getRedisExpire", Long.class, String.class).getAnnotation(Expire.class);
        long start = System.currentTimeMillis();
        ItemObject current = cacheService.getRedisExpire(2L, "id2");
        Cache cache = cacheManager.getCache("test_redis", Duration.of(expire.value(), expire.timeUnit().toChronoUnit()));

        Assertions.assertInstanceOf(RedisCache.class, cache);

        long ttl = redisTemplate.getExpire("sfccmr:test_redis::2:id2");
        long end = System.currentTimeMillis();

        System.out.println(ttl);

        Assertions.assertTrue(ttl + Duration.ofMillis(end-start).get(expire.timeUnit().toChronoUnit()) == expire.value());
    }

    @Test
    void testAssignment () throws NoSuchMethodException, InterruptedException {
        Expire expire = CacheService.class.getMethod("geAssignmentExpire", Long.class, String.class).getAnnotation(Expire.class);
        cacheService.geAssignmentExpire(2L, "id2");
        Cache cache = cacheManager.getCache("test_redis", Duration.of(expire.value(), expire.timeUnit().toChronoUnit()));

        Assertions.assertInstanceOf(CaffeineCache.class, cache);
        expire.timeUnit().sleep(expire.value());
        Assertions.assertNull(cache.get("2:id2"));
    }
}
