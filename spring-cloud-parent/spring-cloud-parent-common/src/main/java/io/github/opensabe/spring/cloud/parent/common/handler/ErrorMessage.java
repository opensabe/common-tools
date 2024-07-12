package io.github.opensabe.spring.cloud.parent.common.handler;

/**
 * ErrorCode 枚举父类
 * @author maheng
 */
@SuppressWarnings("unused")
public interface ErrorMessage {

    /**
     * 错误编码
     * @return success 10000
     */
    int code ();

    /**
     * 错误信息
     * @return  错误信息
     */
    String message ();

}
