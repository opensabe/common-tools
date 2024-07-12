package io.github.opensabe.common.core;

import lombok.Getter;

/**
 * 错误异常公共类
 */
@Getter
public enum ErrorCode {
    /**
     * Standard response code for backend system
     */
    SUCCESS(10000, "success", ""),                          // business success, default successful result
    FAIL(11000, "fail", ""),                                // business failed, which is foreseeable (different from error)
    INVALID(19000, "invalid", ""),                          // request parameter not valid
    RESOURCE_NOT_FOUND(19001, "resource not found", ""),    // resource not found, especially for representing resource manipulation
    BAD_STATE(19002, "bad state", "Sorry，something went wrong."),                      // bad state, especially for representing state illegal
    ERROR(19999, "error", "Sorry，something went wrong."),                              // unexpected system error
    /**
     * Specified BizCode {SrcEnum.id + 4 digits}
     */
    // Pocket
    ACC_BAL_NOT_ENOUGH(61100, "account balance not enough", "The amount has exceeded your available balance, please check and confirm again."),
    ACC_ALREADY_STOP(61200, "account already stop", "This account has been temporarily locked for security concern. If you need any help, please contact us at: XXXXXXX"),
    ACC_ALREADY_FROZEN(61300, "account already frozen", "This account has been temporarily locked for security concern. If you need any help, please contact us at: XXXXXXX"),
    OVER_BANK_DAILY_LIMIT(62100, "over bank daily limit", "Maximum Daily Transaction Value is KSh 140,000.00. The maximum you can send in a day is KSh 140,000.00."),
    OVER_AUDIT_LIMIT(62200, "over audit limit, need admin audit", "The amount exceeds KSh %s, manual process would be applied. You can expect the result in 3 working days. "),
    PAY_PWD_FAILED(63100, "pay password failed", ""),
    OVER_PAY_PWD_DAILY_LIMIT(63200, "over pay password daily limit", ""),
    OVER_PAY_PWD_DAILY_LIMIT_FROZEN(63201, "over pay password daily limit and frozen account", ""),
    PAY_CH_REQUEST_TIMEOUT(64001, "pay channel request timeout", ""),
    PAY_CH_RESULT_TIMEOUT(64002, "pay channel result timeout", ""),
    // Gift
    GIFT_CODE_ALREADY_REDEEMED(73100, "gift code already redeemed", ""),
    GIFT_BAL_NOT_ENOUGH(73200, "gift balance not enough", ""),
    OVER_DAILY_GIFT_REDEEM_LIMIT(73300, "over daily gift redeem limit", ""),
    GIFT_UNAVAILABLE(73400, "gift unavailable", "The gift you have chosen can not be used at this time, please try another one."),
    GIFT_ALREADY_EXPIRED(73401, "gift already expired", "the gift used in this betslip has been expired, please try another one."),
    GIFT_LEAST_ORDER_NOT_MEET(73402, "gift least order not meet", "the current total stake does not meet the requirements of usage, please try another one."),

    /**
     * Other system
     */
    PHONE_ALREADY_REGISTERED(11600, "The mobile number has already been registered.", ""),
    ;

    final int code;         // 错误代码
    final String msg;       // 错误信息
    final String userMsg;   // 用户信息

    ErrorCode(int code, String msg, String userMsg) {
        this.code = code;
        this.msg = msg;
        this.userMsg = userMsg;
    }
}