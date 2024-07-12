package io.github.opensabe.common.redisson.aop;

/**
 * redisson 切点通知
 */
public class RedissonSemaphoreAdvisor extends AbstractRedissonAdvisor<RedissonSemaphoreProperties> {
    public RedissonSemaphoreAdvisor(RedissonSemaphoreCachedPointcut redissonSemaphoreCachedPointcut) {
        super(redissonSemaphoreCachedPointcut);
    }
}
