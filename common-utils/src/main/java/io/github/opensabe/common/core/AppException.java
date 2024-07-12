package io.github.opensabe.common.core;

import lombok.Getter;
import lombok.Setter;

/**
 * APP异常
 */
@Getter
@Setter
public class AppException extends Exception {

    private static final long serialVersionUID = -47818914715891858L;

    /**
     * 错误码
     */
    private ErrorCode errorCode;

    /**
     * 原因信息
     */
    private String causeMsg;

    /**
     * 运输对象
     */
    private Carrier carrier;

    public AppException(ErrorCode errorCode) {
        super(String.format("errCode=%s, msg=%s", errorCode.code, errorCode.msg));
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, Carrier carrier) {
        super(String.format("errCode=%s, msg=%s", errorCode.code, errorCode.msg));
        this.errorCode = errorCode;
        this.carrier = carrier;
    }

    public AppException(ErrorCode errorCode, String causeMsg) {
        super(String.format("errCode=%s, msg=%s, causeMsg=%s", errorCode.code, errorCode.msg, causeMsg));
        this.errorCode = errorCode;
        this.causeMsg = causeMsg;
    }

    public AppException(ErrorCode errorCode, String causeMsgTemplate, Object... objects) {
        super(String.format("errCode=%s, msg=%s, causeMsg=%s", errorCode.code, errorCode.msg, FormatUtil.format(causeMsgTemplate, objects)));
        this.errorCode = errorCode;
        this.causeMsg = FormatUtil.format(causeMsgTemplate, objects);
    }

    public AppException(ErrorCode errorCode, Carrier carrier, String causeMsgTemplate, Object... objects) {
        super(String.format("errCode=%s, msg=%s, causeMsg=%s", errorCode.code, errorCode.msg, FormatUtil.format(causeMsgTemplate, objects)));
        this.errorCode = errorCode;
        this.causeMsg = FormatUtil.format(causeMsgTemplate, objects);
        this.carrier = carrier;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.msg, cause);
        this.errorCode = errorCode;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
