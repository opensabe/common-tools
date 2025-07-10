package io.github.opensabe.common.redisson.aop.lock;

import io.github.opensabe.common.redisson.aop.AbstractRedissonAdvisor;

/**
 * redisson 切点通知
 */
public class RedissonLockAdvisor extends AbstractRedissonAdvisor<RedissonLockProperties> {
    public RedissonLockAdvisor(RedissonLockCachedPointcut redissonLockCachedPointcut) {
        super(redissonLockCachedPointcut);
    }
}
