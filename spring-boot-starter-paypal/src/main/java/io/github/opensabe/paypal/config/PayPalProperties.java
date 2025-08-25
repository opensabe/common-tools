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
package io.github.opensabe.paypal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@ConfigurationProperties(prefix = "paypal.api")
@Data
public class PayPalProperties {

    /**
     * PayPal api的url地址
     */
    @NotBlank
    private String url;

    /**
     * PayPal 的 client_id
     */
    @NotBlank
    private String clientId;

    /**
     * PayPal 的 client secret
     */
    @NotBlank
    private String clientSecret;


    /**
     * PayPal 的 server index 0:sandbox 1:living
     */
    private Integer serverIndex;

    /**
     * PayPal 的 web hook
     */
    private String webhookId;
}
