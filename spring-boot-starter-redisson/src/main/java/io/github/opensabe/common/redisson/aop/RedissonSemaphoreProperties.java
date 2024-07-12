package io.github.opensabe.common.redisson.aop;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedissonSemaphoreProperties extends AbstractRedissonProperties {
    private final RedissonSemaphore redissonSemaphore;
    private final RedissonSemaphoreName redissonSemaphoreName;
    private String semaphoreName;

    public RedissonSemaphoreProperties(RedissonSemaphore redissonSemaphore, RedissonSemaphoreName redissonSemaphoreName, int parameterIndex) {
        super(parameterIndex);
        this.redissonSemaphore = redissonSemaphore;
        this.redissonSemaphoreName = redissonSemaphoreName;
    }
}
