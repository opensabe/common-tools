package io.github.opensabe.base.code;

import lombok.Getter;

/**
 * Business code in basic response
 */
@Getter
public enum BizCodeEnum {
    /**
     * Standard response code for backend system
     */
    SUCCESS(10000, "success"),                          // business success, default successful result
    FAIL(11000, "fail"),                                // business failed, which is foreseeable (different from error)
    INVALID(19000, "invalid"),                          // request parameter not valid
    RESOURCE_NOT_FOUND(19001, "resource not found"),    // resource not found, especially for representing resource manipulation
    BAD_STATE(19002, "bad state"),                      // bad state, especially for representing state illegal
    FORBIDDEN(19003, "Forbidden"),                      // bad state, especially for representing state illegal
    ERROR(19999, "error"),                              // unexpected system error
    /**
     * Specified BizCode {SrcEnum.id + 4 digits}
     */
    ;

    BizCodeEnum(int val, String defaultMsg) {
        this.val = val;
        this.defaultMsg = defaultMsg;
    }

    private int val;
    private String defaultMsg;
}