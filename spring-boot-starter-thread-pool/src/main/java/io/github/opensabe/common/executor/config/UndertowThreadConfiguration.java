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
package io.github.opensabe.common.executor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.ThreadPoolFactoryGracefulShutDownHandler;

/**
 * 注册 {@link ThreadPoolFactory} 优雅关闭处理器。
 * <p>
 * 类名沿用 Undertow 历史命名；Boot 4 已不再内置 Undertow，但处理器仍通过
 * {@code UndertowGracefulShutdownInitializer} 在应用停止阶段被调用。
 */
@Configuration(proxyBeanMethods = false)
public class UndertowThreadConfiguration {

    /**
     * 注册线程池工厂优雅关闭处理器（若容器中尚未存在）。
     *
     * @param threadPoolFactory 线程池工厂
     * @return 优雅关闭处理器
     */
    @Bean
    @ConditionalOnMissingBean(ThreadPoolFactoryGracefulShutDownHandler.class)
    public ThreadPoolFactoryGracefulShutDownHandler threadPoolFactoryGracefulShutDownHandler(ThreadPoolFactory threadPoolFactory) {
        return new ThreadPoolFactoryGracefulShutDownHandler(threadPoolFactory);
    }
}
