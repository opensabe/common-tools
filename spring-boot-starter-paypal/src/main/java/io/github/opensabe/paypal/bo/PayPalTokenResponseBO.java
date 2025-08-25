/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
