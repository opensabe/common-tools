package io.github.opensabe.common.idgenerator.exception;

import java.io.Serial;

/**
 * 17/7/7 下午3:20.
 *
 * @author zhaozhou
 */

public class IdGenerateException extends RuntimeException
{
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 696109974714078172L;

	public IdGenerateException(String msg)
	{
		super(msg);
	}
}
