package io.github.opensabe.common.redisson.aop.lock;

import io.github.opensabe.common.redisson.annotation.RedissonLock;
import io.github.opensabe.common.redisson.aop.old.ExtraNameProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;


public class RedissonLockProperties extends ExtraNameProperties {

    private final RedissonLock redissonLock;

    public RedissonLockProperties(MethodArgumentsExpressEvaluator evaluator, RedissonLock redissonLock) {
        super(evaluator, redissonLock.name(), redissonLock.prefix());
        this.redissonLock = redissonLock;
    }
    @SuppressWarnings("removal")
    public RedissonLockProperties(RedissonLock redissonLock,
                                  io.github.opensabe.common.redisson.annotation.RedissonLockName redissonLockName,
                                  int pamaterIndex) {
        super(redissonLockName.prefix(), redissonLock.name(), pamaterIndex, redissonLockName.expression());
        this.redissonLock = redissonLock;
    }

    public RedissonLock getRedissonLock() {
        return redissonLock;
    }
}
