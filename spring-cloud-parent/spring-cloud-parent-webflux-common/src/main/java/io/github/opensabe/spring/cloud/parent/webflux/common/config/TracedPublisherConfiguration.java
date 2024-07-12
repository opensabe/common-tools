package io.github.opensabe.spring.cloud.parent.webflux.common.config;

import io.github.opensabe.spring.cloud.parent.webflux.common.TracedPublisherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TracedPublisherConfiguration {
    @Bean
    public TracedPublisherFactory tracedPublisherFactory() {
        return new TracedPublisherFactory();
    }
}
