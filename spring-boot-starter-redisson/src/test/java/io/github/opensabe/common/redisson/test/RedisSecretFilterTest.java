package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.test.common.SingleRedisIntegrationTest;
import io.github.opensabe.common.redisson.util.LuaLimitCache;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith({SpringExtension.class, SingleRedisIntegrationTest.class})
@AutoConfigureObservability
@SpringBootTest(properties = {
        //spring-boot 2.6.x 开始，禁止循环依赖（A -> B, B -> A），字段注入一般会导致这种循环依赖，但是我们字段注入太多了，挨个检查太多了
        "spring.main.allow-circular-references=true",
        "spring.data.redis.host=localhost",
        "spring.data.redis.lettuce.pool.enabled=true",
        "spring.data.redis.lettuce.pool.max-active=2",
        "spring.data.redis.port=" + SingleRedisIntegrationTest.PORT,
},classes = RedisSecretFilterTest.App.class)
public class RedisSecretFilterTest {

    @SpringBootApplication
    public static class App {
        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }

    private static final String SECRET = "secretString";

    public static class TestSecretProvider extends SecretProvider {
        protected TestSecretProvider(GlobalSecretManager globalSecretManager) {
            super(globalSecretManager);
        }

        @Override
        protected String name() {
            return "testSecretProvider";
        }

        @Override
        protected long reloadTimeInterval() {
            return 1;
        }

        @Override
        protected TimeUnit reloadTimeIntervalUnit() {
            return TimeUnit.DAYS;
        }

        @Override
        protected Map<String, Set<String>> reload() {
            return Map.of(
                    "testSecretProviderKey", Set.of(SECRET)
            );
        }
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() {
        assertThrows(
                RuntimeException.class,
                () -> stringRedisTemplate.opsForValue().set("test1", SECRET)
        );
        String s = stringRedisTemplate.opsForValue().get("test1");
        Assertions.assertNotEquals(SECRET, s);

        assertThrows(
                RuntimeException.class,
                () -> stringRedisTemplate.opsForHash().put("test2", "testKey", SECRET)
        );
        Assertions.assertNotEquals(SECRET, stringRedisTemplate.opsForHash().get("test2", "testKey"));

        assertThrows(
                RuntimeException.class,
                () -> stringRedisTemplate.opsForList().leftPush("test3", SECRET)
        );
        Assertions.assertNotEquals(SECRET, stringRedisTemplate.opsForList().leftPop("test3"));
    }
}
