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
package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.redisson.util.LuaLimitCache;
import io.github.opensabe.common.secret.GlobalSecretManager;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
public class RedisConfiguration {
    @Bean
    public LuaLimitCache luaLimitCache(StringRedisTemplate redisTemplate, RedissonClient redissonClient) {
        return new LuaLimitCache(redisTemplate, redissonClient);
    }

    @Bean
    public RedisTemplateSecretFilter redisTemplateSecretFilter(GlobalSecretManager globalSecretManager) {
        return new RedisTemplateSecretFilter(globalSecretManager);
    }
}
