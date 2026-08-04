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
package io.github.opensabe.spring.cloud.parent.common.eureka;

import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

/**
 * Eureka 实例注册信息定制 SPI。
 * <p>
 * 实现类注册为 Spring Bean 后，于 {@link EurekaInstanceConfigBean} 初始化完成后被调用。
 */
public interface EurekaInstanceConfigBeanCustomizer {
    /**
     * 修改 Eureka 实例配置。
     *
     * @param eurekaInstanceConfigBean 待定制的实例配置 Bean
     */
    void customize(EurekaInstanceConfigBean eurekaInstanceConfigBean);
}
