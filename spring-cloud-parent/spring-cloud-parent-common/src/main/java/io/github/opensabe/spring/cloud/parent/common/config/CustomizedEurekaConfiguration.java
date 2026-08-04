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

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanAddNodeInfoCustomizer;
import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanCustomizer;
import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanPostProcessor;

/**
 * Eureka 实例注册定制配置。
 * <p>
 * 注册节点/可用区 metadata 注入器与 {@link EurekaInstanceConfigBeanCustomizer} 后置处理器。
 */
@Configuration(proxyBeanMethods = false)
public class CustomizedEurekaConfiguration {

    /**
     * 向 Eureka metadata 注入 K8s 节点名与可用区。
     *
     * @return 节点信息定制器
     */
    @Bean
    public EurekaInstanceConfigBeanAddNodeInfoCustomizer eurekaInstanceConfigBeanAddNodeInfoCustomizer() {
        return new EurekaInstanceConfigBeanAddNodeInfoCustomizer();
    }

    /**
     * 在 {@link org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean} 初始化后应用所有定制器。
     *
     * @param eurekaInstanceConfigBeanCustomizers 已注册的定制器列表
     * @return Bean 后置处理器
     */
    @Bean
    public EurekaInstanceConfigBeanPostProcessor eurekaInstanceConfigBeanPostProcessor(
            List<EurekaInstanceConfigBeanCustomizer> eurekaInstanceConfigBeanCustomizers
    ) {
        return new EurekaInstanceConfigBeanPostProcessor(eurekaInstanceConfigBeanCustomizers);
    }
}
