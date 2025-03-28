package io.github.opensabe.apple;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.verification.SignedDataVerifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "apple.in-purchase.enable", matchIfMissing = false, havingValue = "true")
@EnableConfigurationProperties(AppleInPurchaseProperties.class)
public class AppleInPurchaseConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReceiptUtility receiptUtility() {
        return new ReceiptUtility();
    }

    @Bean
    @ConditionalOnMissingBean
    public AppStoreServerAPIClient appStoreServerAPIClient(AppleInPurchaseProperties appleInPurchaseProperties) {
        String signingKey = appleInPurchaseProperties.getSigningKey();
        String keyId = appleInPurchaseProperties.getKeyId();
        String issuerId = appleInPurchaseProperties.getIssuerId();
        String bundleId = appleInPurchaseProperties.getBundleId();
        Environment environment = appleInPurchaseProperties.getEnvironment();
        return new AppStoreServerAPIClient(signingKey, keyId, issuerId, bundleId, environment);
    }

    public static final Set<String> ROOT_CERTIFICATE_PATH = Set.of("apple/cer/root/AppleComputerRootCertificate.cer",
            "apple/cer/root/AppleIncRootCertificate.cer",
            "apple/cer/root/AppleRootCA-G2.cer",
            "apple/cer/root/AppleRootCA-G3.cer");

    @Bean
    @ConditionalOnMissingBean
    public SignedDataVerifier signedDataVerifier(AppleInPurchaseProperties appleInPurchaseProperties){
        Set<InputStream> rootCertificates = getRootCertificates();
        String bundleId = appleInPurchaseProperties.getBundleId();
        Long appAppleId = appleInPurchaseProperties.getAppAppleId();
        Environment environment = appleInPurchaseProperties.getEnvironment();
        Boolean enableOnlineChecks = appleInPurchaseProperties.getEnableOnlineChecks();
        return new SignedDataVerifier(rootCertificates, bundleId, appAppleId, environment, enableOnlineChecks);
    }

    public static Set<InputStream> getRootCertificates() {
        Set<InputStream> rootCertificates = ROOT_CERTIFICATE_PATH.stream()
                .map(path -> {
                    ClassPathResource classPathResource = new ClassPathResource(path);
                    try {
                        return classPathResource.getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
        return rootCertificates;
    }
}
