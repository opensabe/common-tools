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
package io.github.opensabe.apple;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.apple.itunes.storekit.model.Environment;

/**
 * Apple 内购（App Store Server API）配置属性。
 * <p>
 * 绑定前缀 {@code apple.in-purchase}。
 */
@ConfigurationProperties("apple.in-purchase")
public class AppleInPurchaseProperties {

    /** 是否启用内购自动配置。 */
    private boolean enable = false;

    /** App Store Connect 私钥（PKCS#8 PEM 或 DER Base64）。 */
    private String signingKey;

    /** 私钥 ID（Key ID）。 */
    private String keyId;

    /** 发行者 ID（Issuer ID）。 */
    private String issuerId;

    /** 应用 Bundle ID。 */
    private String bundleId;

    /** 运行环境（Sandbox / Production）。 */
    private Environment environment;

    /** App Store 应用 numeric ID。 */
    private Long appAppleId;

    /** 是否启用 Apple 在线 JWS 校验。 */
    private Boolean enableOnlineChecks = true;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public void setSigningKey(String signingKey) {
        this.signingKey = signingKey;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Long getAppAppleId() {
        return appAppleId;
    }

    public void setAppAppleId(Long appAppleId) {
        this.appAppleId = appAppleId;
    }

    public Boolean getEnableOnlineChecks() {
        return enableOnlineChecks;
    }

    public void setEnableOnlineChecks(Boolean enableOnlineChecks) {
        this.enableOnlineChecks = enableOnlineChecks;
    }
}
