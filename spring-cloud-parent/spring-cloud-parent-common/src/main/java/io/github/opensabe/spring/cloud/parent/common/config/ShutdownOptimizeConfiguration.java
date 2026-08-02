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


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.opensabe.spring.cloud.parent.common.shutdown.GracefulShutdownDelayBuffer;
import lombok.extern.log4j.Log4j2;

/**
 * 优雅关闭优化配置。
 * <p>
 * 注册 {@link io.github.opensabe.spring.cloud.parent.common.shutdown.GracefulShutdownDelayBuffer}，
 * 在 Eureka 下线后等待客户端缓存刷新。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class ShutdownOptimizeConfiguration {

    /**
     * 注册优雅关闭延迟缓冲监听器。
     *
     * @return 延迟缓冲监听器
     */
    @Bean
    public GracefulShutdownDelayBuffer gracefulShutdownDelayBuffer() {
        return new GracefulShutdownDelayBuffer();
    }
}
