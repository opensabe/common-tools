package io.github.opensabe.spring.cloud.parent.webflux.common.config;

import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientRequestCircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerJFRProperties;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerRequestObservationToJFRGenerator;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.WebClientJFRConfigurationProperties;
import io.github.opensabe.spring.cloud.parent.webflux.common.jfr.WebClientObservationToJFRGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.CustomizedReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerClientRequestTransformer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        WebClientConfigurationProperties.class, WebClientJFRConfigurationProperties.class, HttpServerJFRProperties.class
})
public class WebClientConfiguration {
    @Bean
    public WebClientNamedContextFactory getWebClientNamedContextFactory() {
        return new WebClientNamedContextFactory();
    }

    @Bean
    public CircuitBreakerExtractor webClientRequestCircuitBreakerExtractor() {
        return new WebClientRequestCircuitBreakerExtractor();
    }

    @Bean
    public CustomizedReactorLoadBalancerExchangeFilterFunction customizedReactorLoadBalancerExchangeFilterFunction(
            ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory,
            ObjectProvider<List<LoadBalancerClientRequestTransformer>> transformers
    ) {
        return new CustomizedReactorLoadBalancerExchangeFilterFunction(
                loadBalancerFactory, transformers.getIfAvailable()
        );
    }

    @Bean
    public WebClientObservationToJFRGenerator webClientObservationToJFRGenerator(
            WebClientJFRConfigurationProperties properties
    ) {
        return new WebClientObservationToJFRGenerator(properties);
    }

    @Bean
    public HttpServerRequestObservationToJFRGenerator httpServerRequestObservationToJFRGenerator(
            HttpServerJFRProperties properties
    ) {
        return new HttpServerRequestObservationToJFRGenerator(properties);
    }
}
