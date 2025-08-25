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
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.common.preheating.DelayApplicationReadyEventListener;
import io.github.opensabe.spring.cloud.parent.common.preheating.PreheatingProperties;
import lombok.extern.log4j.Log4j2;

/**
 * 预热配置
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PreheatingProperties.class)
public class PreheatingConfiguration {
    @Bean
    @ConditionalOnProperty(value = "preheating.enabled", matchIfMissing = false, havingValue = "true")
    //必须通过 http 暴露 health 端口才启用
    @ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class, exposure = EndpointExposure.WEB)
    public DelayApplicationReadyEventListener delayApplicationReadyEventListener() {
        return new DelayApplicationReadyEventListener();
    }
}
