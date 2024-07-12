package io.github.opensabe.apple;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "apple.login.enable", matchIfMissing = false, havingValue = "true")
@EnableConfigurationProperties(AppleLoginProperties.class)
public class AppleLoginConfiguration {

    @Bean("appleIosLoginAPIClient")
    public AppleLoginAPIClient appleIosLoginAPIClient(AppleLoginProperties appleLoginProperties) {
        AppleLoginProperties.AppleLoginCommonProperties ios = appleLoginProperties.getIos();
        return new AppleLoginAPIClient(ios.getSigningKey(), ios.getKeyId(), ios.getIssuerId(), ios.getBundleId(), ios.getRedirectUri());
    }

    @Bean("appleWebLoginAPIClient")
    public AppleLoginAPIClient appleWebLoginAPIClient(AppleLoginProperties appleLoginProperties) {
        AppleLoginProperties.AppleLoginCommonProperties web = appleLoginProperties.getWeb();
        return new AppleLoginAPIClient(web.getSigningKey(), web.getKeyId(), web.getIssuerId(), web.getBundleId(), web.getRedirectUri());
    }

    @Bean("appleIosLoginUtility")
    public AppleLoginUtility appleIosLoginUtility(@Qualifier("appleIosLoginAPIClient") AppleLoginAPIClient appleLoginAPIClient) {
        return new AppleLoginUtility(appleLoginAPIClient);
    }

    @Bean("appleWebLoginUtility")
    public AppleLoginUtility appleWebLoginUtility(@Qualifier("appleWebLoginAPIClient") AppleLoginAPIClient appleLoginAPIClient) {
        return new AppleLoginUtility(appleLoginAPIClient);
    }

}
