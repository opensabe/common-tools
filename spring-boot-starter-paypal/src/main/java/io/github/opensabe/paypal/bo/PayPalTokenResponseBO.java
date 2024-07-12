package io.github.opensabe.paypal.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接收获取PayPal token 返回值BO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayPalTokenResponseBO {

    /**
     * 状态码
     */
    private Integer code;

    /**
     *
     */
    private String protocol;

    /**
     * 消息
     */
    private String message;

    /**
     * 请求的url
     */
    private String url;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 错误描述
     */
    private String errorDescription;

    /**
     *
     */
    private String scope;

    /**
     * token
     */
    private String accessToken;

    /**
     * 有效时间，秒
     * 每次PayPal创建token有效时间都是9个小时=32400秒
     */
    private Integer expiresIn;

    /**
     * token类型
     */
    private String tokenType;

    /**
     * nonce
     */
    private String nonce;
}
