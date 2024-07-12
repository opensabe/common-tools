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
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RedisTemplate) {
            RedisTemplate redisTemplate = (RedisTemplate) bean;
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

    private static class SecretCheckRedisSerializer<T> implements RedisSerializer<T> {
        private final RedisSerializer<T> delegate;
        private final GlobalSecretManager globalSecretManager;

        public SecretCheckRedisSerializer(RedisSerializer<T> delegate, GlobalSecretManager globalSecretManager) {
            this.delegate = delegate;
            this.globalSecretManager = globalSecretManager;
        }

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

