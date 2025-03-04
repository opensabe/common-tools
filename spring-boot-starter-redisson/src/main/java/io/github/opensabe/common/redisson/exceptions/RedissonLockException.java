package io.github.opensabe.common.redisson.exceptions;

/**
 * @author heng.ma
 */
public class RedissonLockException extends RedissonClientException {

    public RedissonLockException() {
    }

    public RedissonLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedissonLockException(String message) {
        super(message);
    }

    public RedissonLockException(Throwable cause) {
        super(cause);
    }

    public RedissonLockException(Object message) {
        super(message);
    }
}
