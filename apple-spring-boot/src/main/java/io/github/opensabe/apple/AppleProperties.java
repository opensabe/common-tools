package io.github.opensabe.apple;

import com.apple.itunes.storekit.model.Environment;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("apple.in-purchase")
public class AppleProperties {

    private boolean enable = false;

    private String signingKey;

    private String keyId;

    private String issuerId;

    private String bundleId;

    private Environment environment;

    private Long appAppleId;

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
