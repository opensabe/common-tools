package io.github.opensabe.spring.cloud.starter.third.client.conf;

import io.github.opensabe.spring.cloud.starter.third.client.webclient.ThirdPartyWebClientNamedContextFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ThirdPartyWebClientConfigurationProperties.class)
public class ThirdPartyWebClientConfiguration {
    @Bean
    public ThirdPartyWebClientNamedContextFactory getThirdPartyWebClientNamedContextFactory() {
        return new ThirdPartyWebClientNamedContextFactory();
    }
}
