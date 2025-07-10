package io.github.opensabe.common.redisson.exceptions;

public class RedissonClientException extends RuntimeException {

	public RedissonClientException(String message) {
		super(message);
	}

	public RedissonClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
