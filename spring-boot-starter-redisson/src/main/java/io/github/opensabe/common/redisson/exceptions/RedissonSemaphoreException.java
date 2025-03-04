package io.github.opensabe.common.redisson.exceptions;

/**
 * @author heng.ma
 */
public class RedissonSemaphoreException extends RedissonClientException  {
    public RedissonSemaphoreException() {
    }

    public RedissonSemaphoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedissonSemaphoreException(String message) {
        super(message);
    }

    public RedissonSemaphoreException(Throwable cause) {
        super(cause);
    }

    public RedissonSemaphoreException(Object message) {
        super(message);
    }
}
