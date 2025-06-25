package io.github.opensabe.common.redisson.exceptions;

/**
 * @author heng.ma
 */
public class RedissonLockException extends RedissonClientException {


    public RedissonLockException(String message) {
        super(message);
    }
}
