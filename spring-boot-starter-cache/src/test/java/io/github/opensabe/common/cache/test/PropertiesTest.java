package io.github.opensabe.common.cache.test;

import io.github.opensabe.common.cache.config.CachesProperties;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "spring.main.allow-circular-references=true",
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
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.port=6378",
}, classes = App.class)
public class PropertiesTest {
    @ClassRule
    static GenericContainer redisServer = new FixedHostPortGenericContainer("redis")
            .withFixedExposedPort(6378,6379);

    @BeforeAll
    public static void setUp() throws Exception {
        System.out.println("start redis");
        redisServer.start();
        System.out.println("redis started");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        System.out.println("stop redis");
        redisServer.stop();
        System.out.println("redis stopped");
    }

    @Autowired
    private CachesProperties properties;

    @Test
    void testProperties () {
        var list = properties.getCustom();
        var caffeine = list.get(0);
        System.out.println(caffeine.getCaffeine().getSpec());
        Assertions.assertEquals(caffeine.getCaffeine().getSpec(), "expireAfterWrite=5s");
    }
}
