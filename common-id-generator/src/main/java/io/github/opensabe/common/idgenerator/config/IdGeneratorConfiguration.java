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
package io.github.opensabe.common.idgenerator.config;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.idgenerator.service.UniqueIDImpl;
import io.github.opensabe.common.idgenerator.service.UniqueIDWithouBizType;
import io.github.opensabe.common.idgenerator.service.UniqueIDWithoutBizTypeImpl;

@Configuration(proxyBeanMethods = false)
public class IdGeneratorConfiguration {
    @Bean
    public UniqueID getUniqueID(StringRedisTemplate redisTemplate, RedissonClient redissonClient, ThreadPoolFactory threadPoolFactory) {
        return new UniqueIDImpl(redisTemplate, redissonClient, threadPoolFactory);
    }

    @Bean
    public UniqueIDWithouBizType getUniqueIDWithouBizType(StringRedisTemplate redisTemplate, RedissonClient redissonClient, ThreadPoolFactory threadPoolFactory) {
        return new UniqueIDWithoutBizTypeImpl(redisTemplate, redissonClient, threadPoolFactory);
    }
}
