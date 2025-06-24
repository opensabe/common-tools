package io.github.opensabe.common.redisson.aop.ratelimiter;

import io.github.opensabe.common.redisson.annotation.RedissonRateLimiter;
import io.github.opensabe.common.redisson.annotation.RedissonRateLimiterName;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedissonRateLimiterProperties extends AbstractRedissonProperties {
    private final RedissonRateLimiter redissonRateLimiter;
    private final RedissonRateLimiterName redissonRateLimiterName;
    private String rateLimiterName;

    public RedissonRateLimiterProperties(RedissonRateLimiter redissonRateLimiter, RedissonRateLimiterName redissonRateLimiterName, int parameterIndex) {
        super(parameterIndex);
        this.redissonRateLimiter = redissonRateLimiter;
        this.redissonRateLimiterName = redissonRateLimiterName;
    }
}
