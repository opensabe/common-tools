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
package io.github.opensabe.spring.cloud.starter.third.client.conf;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.webclient.observation.ObservationWebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.opensabe.spring.cloud.starter.third.client.webclient.ThirdPartyWebClientNamedContextFactory;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.log4j.Log4j2;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;

/**
 * 第三方 HTTP 调用 WebClient 默认配置。
 * <p>
 * 按 {@link ThirdPartyWebClientNamedContextFactory} 上下文名称读取
 * {@link ThirdPartyWebClientConfigurationProperties}，创建直连第三方 baseUrl 的 {@link WebClient}，
 * 不含服务发现与 Resilience4j 组件。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class ThirdPartyWebClientDefaultConfiguration {

    /**
     * 按命名上下文创建第三方 {@link WebClient}。
     * <p>
     * 必须配置 {@code third-party.webclient.configs.<name>.base-url}；
     * 可选 {@code service-name}，缺省时使用配置 key。
     *
     * @param webClientConfigurationProperties 第三方 WebClient 配置属性
     * @param environment                      Spring 环境
     * @param observationWebClientCustomizer   Observation 定制器
     * @return 配置完成的 WebClient
     * @see org.springframework.boot.actuate.autoconfigure.observation.web.client.HttpClientObservationsAutoConfiguration
     */
    @Bean
    public WebClient getWebClient(
            ThirdPartyWebClientConfigurationProperties webClientConfigurationProperties,
            Environment environment,
            ObservationWebClientCustomizer observationWebClientCustomizer
    ) {
        String name = environment.getProperty(ThirdPartyWebClientNamedContextFactory.PROPERTY_NAME);
        Map<String, ThirdPartyWebClientConfigurationProperties.WebClientProperties> configs = webClientConfigurationProperties.getConfigs();
        if (configs == null || configs.size() == 0) {
            throw new BeanCreationException("Failed to create webClient, please provide configurations under namespace: third-party.webclient.configs");
        }
        ThirdPartyWebClientConfigurationProperties.WebClientProperties webClientProperties = configs.get(name);
        if (webClientProperties == null) {
            throw new BeanCreationException("Failed to create webClient, please provide configurations under namespace: third-party.webclient.configs." + name);
        }
        String serviceName = webClientProperties.getServiceName();
        // 若未填写微服务名称，则使用配置 key 作为名称
        if (StringUtils.isBlank(serviceName)) {
            serviceName = name;
        }
        String baseUrl = webClientProperties.getBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            throw new BeanCreationException("Failed to create webClient, base-url not provided." + name);
        }

        HttpClient httpClient = HttpClient
                .create()
                // 跟随重定向
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) webClientProperties.getConnectTimeout().toMillis())
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler((int) webClientProperties.getResponseTimeout().toSeconds()))
                                .addHandlerLast(new WriteTimeoutHandler((int) webClientProperties.getResponseTimeout().toSeconds()))
                )
                .observe(ConnectionObserver.emptyListener())
                // 开启请求压缩
                .compress(true);
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl);
        observationWebClientCustomizer.customize(builder);
        return builder.build();
    }
}
