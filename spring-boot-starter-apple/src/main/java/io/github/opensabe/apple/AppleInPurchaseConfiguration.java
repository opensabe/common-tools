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

/**
 * Apple 内购（App Store Server API）Spring 配置。
 * <p>
 * 在 {@code apple.in-purchase.enable=true} 时注册收据工具、API 客户端与 JWS 校验器。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "apple.in-purchase.enable", matchIfMissing = false, havingValue = "true")
@EnableConfigurationProperties(AppleInPurchaseProperties.class)
public class AppleInPurchaseConfiguration {

    /** Apple 根证书 classpath 路径集合。 */
    public static final Set<String> ROOT_CERTIFICATE_PATH = Set.of("apple/cer/root/AppleComputerRootCertificate.cer",
            "apple/cer/root/AppleIncRootCertificate.cer",
            "apple/cer/root/AppleRootCA-G2.cer",
            "apple/cer/root/AppleRootCA-G3.cer");

    /**
     * 加载 Apple 根证书输入流集合，供 {@link SignedDataVerifier} 使用。
     *
     * @return 根证书 {@link InputStream} 集合
     */
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

    /**
     * 收据解析工具 Bean。
     *
     * @return {@link ReceiptUtility} 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ReceiptUtility receiptUtility() {
        return new ReceiptUtility();
    }

    /**
     * App Store Server API 客户端 Bean。
     *
     * @param appleInPurchaseProperties 内购配置属性
     * @return {@link AppStoreServerAPIClient} 实例
     */
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

    /**
     * 签名数据（JWS）校验器 Bean。
     *
     * @param appleInPurchaseProperties 内购配置属性
     * @return {@link SignedDataVerifier} 实例
     */
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
