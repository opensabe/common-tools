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

import io.github.opensabe.common.executor.GracefulShutdownHandler;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.cloud.parent.web.common.handler.SecretCheckResponseAdvice;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerJFRProperties;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestObservationToJFRGenerator;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.UndertowGracefulShutdownInitializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

/**
 * Web 服务器通用配置。
 * <p>
 * 注册 MVC 扩展、优雅关闭处理器链、HTTP 请求 JFR 观测与响应体密钥校验等 Bean。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({HttpServerJFRProperties.class})
public class WebServerConfiguration {

    /**
     * 注册 Spring MVC 通用配置。
     *
     * @return WebMvc 配置实例
     */
    @Bean
    public WebMvcConfig webMvcConfig() {
        return new WebMvcConfig();
    }

    /**
     * 提供占位 {@link GracefulShutdownHandler}，保证注入列表非空。
     * <p>
     * 该处理器优先级最低且不做任何操作；业务方可注册更高优先级的处理器参与关闭流程。
     *
     * @return 空实现的默认优雅关闭处理器
     */
    @Bean
    public GracefulShutdownHandler defaultGracefulShutdownHandler() {
        return new GracefulShutdownHandler() {
            @Override
            public int getOrder() {
                return Ordered.LOWEST_PRECEDENCE;
            }

            @Override
            public void gracefullyShutdown() {
                // do nothing
            }
        };
    }

    /**
     * 注册 Web 服务器优雅关闭初始化器。
     * <p>
     * 在 Boot Web 服务器排空完成后，按顺序调用所有 {@link GracefulShutdownHandler}。
     *
     * @param gracefulShutdownHandlers 优雅关闭处理器列表
     * @return Undertow 兼容命名的关闭初始化器
     */
    @Bean
    public UndertowGracefulShutdownInitializer undertowGracefulShutdownInitializer(
            List<GracefulShutdownHandler> gracefulShutdownHandlers) {
        return new UndertowGracefulShutdownInitializer(gracefulShutdownHandlers);
    }

    /**
     * 注册 HTTP 请求 Observation 到 JFR 事件的生成器。
     *
     * @param properties HTTP 服务器 JFR 配置属性
     * @return JFR 事件生成器
     */
    @Bean
    public HttpServerRequestObservationToJFRGenerator httpServerRequestObservationToJFRGenerator(
            HttpServerJFRProperties properties) {
        return new HttpServerRequestObservationToJFRGenerator(properties);
    }

    /**
     * 注册响应体密钥泄露检查 Advice。
     *
     * @param globalSecretManager 全局密钥管理器
     * @return 密钥校验响应 Advice
     */
    @Bean
    public SecretCheckResponseAdvice secretCheckResponseAdvice(GlobalSecretManager globalSecretManager) {
        return new SecretCheckResponseAdvice(globalSecretManager);
    }

    // 这里原来有 JFRFilter，用于通过 JFR 记录每个 HTTP 请求的详细信息
    // 现在已经不需要了，因为我们已经将 servlet 线程池替换成自己的 Factory 封装的线程池，而这个线程池已经会记录每个请求
    // 并且，更好的方式应该是通过 micrometer 的 Observation 去记录每个请求的详细信息，之后消费 Observation 生成 JFR 事件
    // 参考：org.springframework.web.filter.ServerHttpObservationFilter
}
