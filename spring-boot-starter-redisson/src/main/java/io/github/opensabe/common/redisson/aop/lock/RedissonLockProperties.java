package io.github.opensabe.common.redisson.aop.lock;

import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.annotation.RedissonLockName;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RedissonLockProperties extends AbstractRedissonProperties {
    private final RedissonLock redissonLock;
    private final RedissonLockName redissonLockName;
    private String lockName;

    public RedissonLockProperties(RedissonLock redissonLock, RedissonLockName redissonLockName, int parameterIndex) {
        super(parameterIndex);
        this.redissonLock = redissonLock;
        this.redissonLockName = redissonLockName;
    }
}
