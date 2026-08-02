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
package io.github.opensabe.common.redisson.config;

import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.ObservedRedissonClient;

/**
 * 将容器中的 {@link RedissonClient} 替换为带 Micrometer 观测的 {@link ObservedRedissonClient}。
 * <p>
 * Redisson 客户端创建较早，须通过 {@link Ordered} 确保本后处理器先于依赖方执行。
 */
public class RedissonClientBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, Ordered {

    /** Spring 应用上下文，延迟获取 {@link UnifiedObservationFactory}。 */
    private ApplicationContext applicationContext;

    /** {@inheritDoc} */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /** {@inheritDoc} — 包装 {@link RedissonClient} 为 {@link ObservedRedissonClient}。 */
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof RedissonClient delegate) {
            return new ObservedRedissonClient(delegate, applicationContext.getBean(UnifiedObservationFactory.class));
        }
        return bean;
    }

    /** {@inheritDoc} — 尽早执行以覆盖早期创建的 RedissonClient。 */
    @Override
    public int getOrder() {
        return 0;
    }
}
