package io.github.opensabe.common.redisson.aop.semaphore;

import io.github.opensabe.common.redisson.annotation.RedissonSemaphore;
import io.github.opensabe.common.redisson.annotation.RedissonSemaphoreName;
import io.github.opensabe.common.redisson.aop.old.ExtraNameProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.Getter;

public class RedissonSemaphoreProperties extends ExtraNameProperties {
    @Getter
    private final RedissonSemaphore redissonSemaphore;

    public RedissonSemaphoreProperties(RedissonSemaphore redissonSemaphore, RedissonSemaphoreName redissonSemaphoreName, int parameterIndex) {
        super(redissonSemaphoreName.prefix(), redissonSemaphore.name(), parameterIndex, redissonSemaphoreName.expression());
        this.redissonSemaphore = redissonSemaphore;
    }
    public RedissonSemaphoreProperties(MethodArgumentsExpressEvaluator evaluator, RedissonSemaphore redissonSemaphore) {
        super(evaluator, redissonSemaphore.prefix(), redissonSemaphore.name());
        this.redissonSemaphore = redissonSemaphore;
    }
}
