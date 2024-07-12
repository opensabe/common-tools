package io.github.opensabe.common.redisson.exceptions;

@SuppressWarnings("serial")
public class RedissonClientException extends RuntimeException
{

	public RedissonClientException()
	{
		super();
	}

	public RedissonClientException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public RedissonClientException(String message)
	{
		super(message);
	}

	public RedissonClientException(Throwable cause)
	{
		super(cause);
	}

	public RedissonClientException(Object message)
	{
		super(message.toString());
	}
}
