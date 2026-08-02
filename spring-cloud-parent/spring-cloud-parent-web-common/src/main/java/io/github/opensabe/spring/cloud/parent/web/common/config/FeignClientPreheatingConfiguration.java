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
package io.github.opensabe.spring.cloud.parent.web.common.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.web.common.feign.preheating.FeignClientPreheatingApplicationReadyEventListener;
import lombok.extern.log4j.Log4j2;

/**
 * Feign 客户端预热配置。
 * <p>
 * 在 {@code FeignClientPreheating.enabled=true} 且 Health 端点已通过 Web 暴露时，
 * 注册应用就绪后的 Feign 预热监听器。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class FeignClientPreheatingConfiguration {

    /**
     * 注册 Feign 客户端预热监听器。
     * <p>
     * 依赖 Web 暴露的 {@link HealthEndpoint}，以便在就绪阶段探测依赖服务。
     *
     * @return Feign 预热应用就绪事件监听器
     */
    @Bean
    @ConditionalOnProperty(value = "FeignClientPreheating.enabled", matchIfMissing = false, havingValue = "true")
    @ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class, exposure = EndpointExposure.WEB)
    public FeignClientPreheatingApplicationReadyEventListener feignClientPreheatingApplicationReadyEventListener() {
        return new FeignClientPreheatingApplicationReadyEventListener();
    }
}
