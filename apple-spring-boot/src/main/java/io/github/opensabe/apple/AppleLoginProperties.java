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
