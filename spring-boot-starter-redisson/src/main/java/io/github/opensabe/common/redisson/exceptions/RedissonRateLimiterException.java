package io.github.opensabe.common.redisson.exceptions;

/**
 * @author heng.ma
 */
public class RedissonRateLimiterException extends RedissonClientException {

    public RedissonRateLimiterException(String message) {
        super(message);
    }
}
