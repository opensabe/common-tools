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

/**
 * Sign In with Apple 配置属性。
 * <p>
 * 绑定前缀 {@code apple.login}，分别配置 Web 与 iOS 客户端。
 */
@ConfigurationProperties("apple.login")
public class AppleLoginProperties {

    /** 是否启用 Apple 登录自动配置。 */
    private boolean enable = false;

    /** Web 端登录配置。 */
    private AppleLoginCommonProperties web;

    /** iOS 端登录配置。 */
    private AppleLoginCommonProperties ios;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public AppleLoginCommonProperties getWeb() {
        return web;
    }

    public void setWeb(AppleLoginCommonProperties web) {
        this.web = web;
    }

    public AppleLoginCommonProperties getIos() {
        return ios;
    }

    public void setIos(AppleLoginCommonProperties ios) {
        this.ios = ios;
    }

    /**
     * Web / iOS 共用的 Apple 登录密钥与客户端配置。
     */
    public static class AppleLoginCommonProperties {

        /** ES256 私钥材料。 */
        private String signingKey;

        /** 私钥 ID。 */
        private String keyId;

        /** Team / 发行者 ID。 */
        private String issuerId;

        /** 应用 Bundle ID（OAuth client_id）。 */
        private String bundleId;

        /** OAuth redirect_uri（Web 端必填）。 */
        private String redirectUri;

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

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }
    }
}
