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
package io.github.opensabe.spring.cloud.parent.common.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.common.preheating.DelayApplicationReadyEventListener;
import io.github.opensabe.spring.cloud.parent.common.preheating.PreheatingProperties;
import lombok.extern.log4j.Log4j2;

/**
 * 应用预热配置。
 * <p>
 * 在 {@code preheating.enabled=true} 且 Health 端点已通过 Web 暴露时，
 * 注册延迟 {@link org.springframework.boot.context.event.ApplicationReadyEvent} 完成的预热监听器。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PreheatingProperties.class)
public class PreheatingConfiguration {

    /**
     * 注册应用就绪延迟预热监听器。
     * <p>
     * 要求 Health 端点 Web 暴露，以便在预热阶段对本机发起健康检查请求。
     *
     * @return 延迟 ApplicationReady 事件监听器
     */
    @Bean
    @ConditionalOnProperty(value = "preheating.enabled", matchIfMissing = false, havingValue = "true")
    @ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class, exposure = EndpointExposure.WEB)
    public DelayApplicationReadyEventListener delayApplicationReadyEventListener() {
        return new DelayApplicationReadyEventListener();
    }
}
