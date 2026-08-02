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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

import io.github.opensabe.apple.appstoreconnectapi.AppleStoreConnectConfiguration;

/**
 * Apple Starter 根自动配置。
 * <p>
 * 聚合内购、App Store Connect API 与 Sign In with Apple 三类子配置。
 */
@AutoConfiguration
@Import({AppleInPurchaseConfiguration.class, AppleStoreConnectConfiguration.class, AppleLoginConfiguration.class})
public class AppleAutoConfiguration {
}
