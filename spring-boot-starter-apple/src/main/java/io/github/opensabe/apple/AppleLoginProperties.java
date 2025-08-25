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

@ConfigurationProperties("apple.login")
public class AppleLoginProperties {

    private boolean enable = false;

    private AppleLoginCommonProperties web;

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

    public static class AppleLoginCommonProperties {
        private String signingKey;

        private String keyId;

        private String issuerId;

        private String bundleId;

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
