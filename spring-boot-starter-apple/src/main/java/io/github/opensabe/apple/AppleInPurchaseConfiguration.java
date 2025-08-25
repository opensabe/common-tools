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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.apple.itunes.storekit.client.AppStoreServerAPIClient;
import com.apple.itunes.storekit.migration.ReceiptUtility;
import com.apple.itunes.storekit.model.Environment;
import com.apple.itunes.storekit.verification.SignedDataVerifier;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "apple.in-purchase.enable", matchIfMissing = false, havingValue = "true")
@EnableConfigurationProperties(AppleInPurchaseProperties.class)
public class AppleInPurchaseConfiguration {

    public static final Set<String> ROOT_CERTIFICATE_PATH = Set.of("apple/cer/root/AppleComputerRootCertificate.cer",
            "apple/cer/root/AppleIncRootCertificate.cer",
            "apple/cer/root/AppleRootCA-G2.cer",
            "apple/cer/root/AppleRootCA-G3.cer");

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

    @Bean
    @ConditionalOnMissingBean
    public SignedDataVerifier signedDataVerifier(AppleInPurchaseProperties appleInPurchaseProperties) {
        Set<InputStream> rootCertificates = getRootCertificates();
        String bundleId = appleInPurchaseProperties.getBundleId();
        Long appAppleId = appleInPurchaseProperties.getAppAppleId();
        Environment environment = appleInPurchaseProperties.getEnvironment();
        Boolean enableOnlineChecks = appleInPurchaseProperties.getEnableOnlineChecks();
        return new SignedDataVerifier(rootCertificates, bundleId, appAppleId, environment, enableOnlineChecks);
    }
}
