package io.github.opensabe.common.redisson.aop;

/**
 * redisson 切点通知
 */
public class RedissonRateLimiterAdvisor extends AbstractRedissonAdvisor<RedissonRateLimiterProperties> {
    public RedissonRateLimiterAdvisor(RedissonRateLimiterCachedPointcut redissonRateLimiterCachedPointcut) {
        super(redissonRateLimiterCachedPointcut);
    }
}
