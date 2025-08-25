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

import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

public class RedisTemplateSecretFilter implements BeanPostProcessor {
    private final GlobalSecretManager globalSecretManager;

    public RedisTemplateSecretFilter(GlobalSecretManager globalSecretManager) {
        this.globalSecretManager = globalSecretManager;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RedisTemplate redisTemplate) {
            RedisSerializer defaultSerializer = redisTemplate.getDefaultSerializer();
            if (defaultSerializer != null) {
                redisTemplate.setDefaultSerializer(new SecretCheckRedisSerializer(defaultSerializer, globalSecretManager));
            }
            RedisSerializer stringSerializer = redisTemplate.getStringSerializer();
            if (stringSerializer != null) {
                redisTemplate.setStringSerializer(new SecretCheckRedisSerializer(stringSerializer, globalSecretManager));
            }
            RedisSerializer hashKeySerializer = redisTemplate.getHashKeySerializer();
            if (hashKeySerializer != null) {
                redisTemplate.setHashKeySerializer(new SecretCheckRedisSerializer(hashKeySerializer, globalSecretManager));
            }
            RedisSerializer hashValueSerializer = redisTemplate.getHashValueSerializer();
            if (hashValueSerializer != null) {
                redisTemplate.setHashValueSerializer(new SecretCheckRedisSerializer(hashValueSerializer, globalSecretManager));
            }
            RedisSerializer keySerializer = redisTemplate.getKeySerializer();
            if (keySerializer != null) {
                redisTemplate.setKeySerializer(new SecretCheckRedisSerializer(keySerializer, globalSecretManager));
            }
            RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
            if (valueSerializer != null) {
                redisTemplate.setValueSerializer(new SecretCheckRedisSerializer(valueSerializer, globalSecretManager));
            }
        }
        return bean;
    }

    private record SecretCheckRedisSerializer<T>(RedisSerializer<T> delegate,
                                                 GlobalSecretManager globalSecretManager) implements RedisSerializer<T> {

        @Override
            public byte[] serialize(T o) throws SerializationException {
                byte[] serialize = delegate.serialize(o);
                if (serialize != null) {
                    String s = new String(serialize, StandardCharsets.UTF_8);
                    FilterSecretStringResult filterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(s);
                    if (filterSecretStringResult.isFoundSensitiveString()) {
                        throw new SerializationException("Sensitive string found in Redis request");
                    }
                }
                return serialize;
            }

            @Override
            public T deserialize(byte[] bytes) throws SerializationException {
                return delegate.deserialize(bytes);
            }
        }
}

