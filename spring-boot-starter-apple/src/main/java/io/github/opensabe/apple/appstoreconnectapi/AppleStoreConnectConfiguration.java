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
package io.github.opensabe.apple.appstoreconnectapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * App Store Connect API Spring 配置。
 * <p>
 * 在 {@code apple.store.connect.enable=true} 时注册 {@link AppleStoreConnectAPIClient}。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "apple.store.connect.enable", matchIfMissing = false, havingValue = "true")
@EnableConfigurationProperties(AppleStoreConnectProperties.class)
public class AppleStoreConnectConfiguration {

    /**
     * App Store Connect API 客户端 Bean。
     *
     * @param appleStoreConnectProperties Store Connect 配置属性
     * @return {@link AppleStoreConnectAPIClient} 实例
     */
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
