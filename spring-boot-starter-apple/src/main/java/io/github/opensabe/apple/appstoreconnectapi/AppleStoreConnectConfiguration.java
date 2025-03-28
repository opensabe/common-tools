package io.github.opensabe.apple.appstoreconnectapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "apple.store.connect.enable", matchIfMissing = false, havingValue = "true")
@EnableConfigurationProperties(AppleStoreConnectProperties.class)
public class AppleStoreConnectConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppleStoreConnectAPIClient appleStoreConnectAPIClient(AppleStoreConnectProperties appleStoreConnectProperties) {
        String signingKey = appleStoreConnectProperties.getSigningKey();
        String keyId = appleStoreConnectProperties.getKeyId();
        String issuerId = appleStoreConnectProperties.getIssuerId();
        String bundleId = appleStoreConnectProperties.getBundleId();
        Long appAppleId = appleStoreConnectProperties.getAppAppleId();
        Assert.notNull(appAppleId, "apple id is null");
        return new AppleStoreConnectAPIClient(signingKey, keyId, issuerId, bundleId, appAppleId);
    }
}
