package io.github.opensabe.common.redisson.exceptions;

/**
 * @author heng.ma
 */
public class RedissonRateLimiterException extends RedissonClientException {
    public RedissonRateLimiterException() {
    }

    public RedissonRateLimiterException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedissonRateLimiterException(String message) {
        super(message);
    }

    public RedissonRateLimiterException(Throwable cause) {
        super(cause);
    }

    public RedissonRateLimiterException(Object message) {
        super(message);
    }
}
