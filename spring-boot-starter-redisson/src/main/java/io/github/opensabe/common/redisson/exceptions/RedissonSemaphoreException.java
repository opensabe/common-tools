package io.github.opensabe.common.redisson.exceptions;

/**
 * @author heng.ma
 */
public class RedissonSemaphoreException extends RedissonClientException  {

    public RedissonSemaphoreException(String message) {
        super(message);
    }

}
