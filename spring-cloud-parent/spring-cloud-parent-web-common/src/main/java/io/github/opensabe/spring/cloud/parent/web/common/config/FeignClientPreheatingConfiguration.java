package io.github.opensabe.spring.cloud.parent.web.common.config;

import io.github.opensabe.spring.cloud.parent.web.common.feign.preheating.FeignClientPreheatingApplicationReadyEventListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration(proxyBeanMethods = false)
public class FeignClientPreheatingConfiguration {
    @Bean
    @ConditionalOnProperty(value = "FeignClientPreheating.enabled", matchIfMissing = false, havingValue = "true")
    @ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class, exposure = EndpointExposure.WEB)
    public FeignClientPreheatingApplicationReadyEventListener feignClientPreheatingApplicationReadyEventListener(){
        return new FeignClientPreheatingApplicationReadyEventListener();
    }
}
