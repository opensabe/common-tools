package io.github.opensabe.common.idgenerator.config;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.idgenerator.service.UniqueIDImpl;
import io.github.opensabe.common.idgenerator.service.UniqueIDWithouBizType;
import io.github.opensabe.common.idgenerator.service.UniqueIDWithoutBizTypeImpl;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

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
