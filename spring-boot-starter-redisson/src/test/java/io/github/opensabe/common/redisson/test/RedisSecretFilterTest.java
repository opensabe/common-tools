package io.github.opensabe.common.redisson.test;

import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(RedisSecretFilterTest.TestConfig.class)
public class RedisSecretFilterTest extends BaseRedissonTest {

    private static final String SECRET = "secretString";

    public static class TestConfig {
        @Bean
        public TestSecretProvider testSecretProvider(GlobalSecretManager globalSecretManager) {
            return new TestSecretProvider(globalSecretManager);
        }
    }


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
