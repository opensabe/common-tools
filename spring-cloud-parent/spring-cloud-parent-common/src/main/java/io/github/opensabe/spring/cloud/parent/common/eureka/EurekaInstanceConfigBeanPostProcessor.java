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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

import java.util.List;

/**
 * 实现 EurekaInstanceConfigBeanCustomizer 在 EurekaInstanceConfigBean 初始化之后修改
 */
public class EurekaInstanceConfigBeanPostProcessor implements BeanPostProcessor {
    private final List<EurekaInstanceConfigBeanCustomizer> eurekaInstanceConfigBeanCustomizers;

    public EurekaInstanceConfigBeanPostProcessor(List<EurekaInstanceConfigBeanCustomizer> eurekaInstanceConfigBeanCustomizers) {
        this.eurekaInstanceConfigBeanCustomizers = eurekaInstanceConfigBeanCustomizers;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof EurekaInstanceConfigBean) {
            EurekaInstanceConfigBean eurekaInstanceConfigBean = (EurekaInstanceConfigBean) bean;
            eurekaInstanceConfigBeanCustomizers.forEach(eurekaInstanceConfigBeanCustomizer -> {
                eurekaInstanceConfigBeanCustomizer.customize(eurekaInstanceConfigBean);
            });
        }
        return bean;
    }
}
