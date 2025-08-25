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
@ConditionalOnProperty(value = "apple.in-purchase.enable", matchIfMissing = false)
@EnableConfigurationProperties(AppleProperties.class)
public class AppleConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReceiptUtility receiptUtility() {
        return new ReceiptUtility();
    }

    @Bean
    @ConditionalOnMissingBean
    public AppStoreServerAPIClient appStoreServerAPIClient(AppleProperties appleProperties) {
        String signingKey = appleProperties.getSigningKey();
        String keyId = appleProperties.getKeyId();
        String issuerId = appleProperties.getIssuerId();
        String bundleId = appleProperties.getBundleId();
        Environment environment = appleProperties.getEnvironment();
        return new AppStoreServerAPIClient(signingKey, keyId, issuerId, bundleId, environment);
    }

    public static final Set<String> ROOT_CERTIFICATE_PATH = Set.of("apple/cer/root/AppleComputerRootCertificate.cer",
            "apple/cer/root/AppleIncRootCertificate.cer",
            "apple/cer/root/AppleRootCA-G2.cer",
            "apple/cer/root/AppleRootCA-G3.cer");

    @Bean
    @ConditionalOnMissingBean
    public SignedDataVerifier signedDataVerifier(AppleProperties appleProperties){
        Set<InputStream> rootCertificates = getRootCertificates();
        String bundleId = appleProperties.getBundleId();
        Long appAppleId = appleProperties.getAppAppleId();
        Environment environment = appleProperties.getEnvironment();
        Boolean enableOnlineChecks = appleProperties.getEnableOnlineChecks();
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
