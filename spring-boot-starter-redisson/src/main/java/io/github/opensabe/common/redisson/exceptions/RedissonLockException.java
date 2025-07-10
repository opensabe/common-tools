package io.github.opensabe.common.redisson.exceptions;

/**
 * @author heng.ma
 */
public class RedissonLockException extends RedissonClientException {


    public RedissonLockException(String message) {
        super(message);
    }
    public RedissonLockException(String message, Throwable e) {
        super(message, e);
    }
}
