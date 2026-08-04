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
package io.github.opensabe.spring.cloud.parent.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gateway JFR 事件配置属性。
 * <p>
 * 绑定前缀 {@code spring.cloud.gateway.jfr}。
 */
@Data
@NoArgsConstructor
@ConfigurationProperties("spring.cloud.gateway.jfr")
public class GatewayJFRProperties {
    /**
     * 是否启用 Gateway JFR 事件，默认 {@code true}。
     */
    private boolean enabled = true;
}
