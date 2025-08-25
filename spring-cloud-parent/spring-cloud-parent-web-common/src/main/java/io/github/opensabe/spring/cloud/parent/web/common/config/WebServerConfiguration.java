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

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerJFRProperties;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestObservationToJFRGenerator;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.DefaultWebServerFactoryCustomizer;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.UndertowGracefulShutdownHandler;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.UndertowGracefulShutdownInitializer;
import io.github.opensabe.spring.cloud.parent.web.common.handler.SecretCheckResponseAdvice;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.undertow.ConfigurableUndertowWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({HttpServerJFRProperties.class})
public class WebServerConfiguration {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableUndertowWebServerFactory> undertowWebServerAccessLogTimingEnabler(ServerProperties serverProperties) {
        return new DefaultWebServerFactoryCustomizer(serverProperties);
    }


    @Bean
    //仅仅是为了保证项目中至少有一个 UndertowGracefulShutdownHandler
    public UndertowGracefulShutdownHandler defaultUndertowGracefulShutdownHandler() {
        return new UndertowGracefulShutdownHandler() {
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

    @Bean
    public UndertowGracefulShutdownInitializer undertowGracefulShutdownInitializer(List<UndertowGracefulShutdownHandler> undertowGracefulShutdownHandlers) {
        return new UndertowGracefulShutdownInitializer(undertowGracefulShutdownHandlers);
    }

    @Bean
    public HttpServerRequestObservationToJFRGenerator httpServerRequestObservationToJFRGenerator(HttpServerJFRProperties properties) {
        return new HttpServerRequestObservationToJFRGenerator(properties);
    }

    @Bean
    public SecretCheckResponseAdvice secretCheckResponseAdvice(GlobalSecretManager globalSecretManager) {
        return new SecretCheckResponseAdvice(globalSecretManager);
    }

    //这里原来有 JFRFilter，用于通过 JFR 记录每个 HTTP 请求的详细信息
    //现在已经不需要了，因为我们已经将 servlet 线程池替换成自己的 Factory 封装的线程池，而这个线程池已经会记录每个请求
    //并且，更好的方式应该是通过 micrometer 的 Observation 去记录每个请求的详细信息，之后消费 Observation 生成 JFR 事件
    //参考：org.springframework.web.filter.ServerHttpObservationFilter
}
