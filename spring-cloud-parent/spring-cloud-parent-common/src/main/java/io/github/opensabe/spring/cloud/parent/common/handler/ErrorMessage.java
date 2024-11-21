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

    /**
     * 有些错误提示需要返回数据
     * @return  BaseRsp中的data
     */
    default Object data () {
        return null;
    };

}
