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
import org.springframework.boot.actuate.metrics.web.reactive.client.ObservationWebClientCustomizer;
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

@Log4j2
@Configuration(proxyBeanMethods = false)
public class ThirdPartyWebClientDefaultConfiguration {

    /**
     * @param webClientConfigurationProperties
     * @param environment
     * @param observationWebClientCustomizer
     * @return
     * @see org.springframework.boot.actuate.autoconfigure.observation.web.client.HttpClientObservationsAutoConfiguration
     */
//    @Bean
//    public ObservationWebClientCustomizer observationWebClientCustomizer () {
//        return new ObservationWebClientCustomizer(unifiedObservationFactory.getObservationRegistry(), new DefaultClientRequestObservationConvention());
//    }
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
        //如果没填写微服务名称，就使用配置 key 作为微服务名称
        if (StringUtils.isBlank(serviceName)) {
            serviceName = name;
        }
        String baseUrl = webClientProperties.getBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            throw new BeanCreationException("Failed to create webClient, base-url not provided." + name);
        }

        HttpClient httpClient = HttpClient
                .create()
                //跟随重定向
                .followRedirect(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) webClientProperties.getConnectTimeout().toMillis())
                .doOnConnected(connection ->
                        connection
                                .addHandlerLast(new ReadTimeoutHandler((int) webClientProperties.getResponseTimeout().toSeconds()))
                                .addHandlerLast(new WriteTimeoutHandler((int) webClientProperties.getResponseTimeout().toSeconds()))
                )
                .observe(ConnectionObserver.emptyListener())
                //开启请求压缩
                .compress(true);
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl);
        observationWebClientCustomizer.customize(builder);
        return builder.build();
    }
}
