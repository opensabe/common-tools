package io.github.opensabe.common.redisson.aop.ratelimiter;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;
import io.github.opensabe.common.redisson.aop.old.ExtraNameProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import lombok.Getter;

public class RedissonRateLimiterProperties extends ExtraNameProperties {
    @Getter
    private final RedissonRateLimiter redissonRateLimiter;

    public RedissonRateLimiterProperties(RedissonRateLimiter redissonRateLimiter, RedissonRateLimiterName redissonRateLimiterName, int parameterIndex) {
        super(redissonRateLimiterName.prefix(), redissonRateLimiter.name(), parameterIndex, redissonRateLimiterName.expression());
        this.redissonRateLimiter = redissonRateLimiter;
    }
    public RedissonRateLimiterProperties(MethodArgumentsExpressEvaluator evaluator, RedissonRateLimiter redissonRateLimiter) {
        super(evaluator, redissonRateLimiter.prefix(), redissonRateLimiter.name());
        this.redissonRateLimiter = redissonRateLimiter;
    }

}
