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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.common.redisson.test.common.BaseRedissonTest;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.secret.SecretProvider;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(RedisSecretFilterTest.TestConfig.class)
public class RedisSecretFilterTest extends BaseRedissonTest {

    private static final String SECRET = "secretString";
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
}
