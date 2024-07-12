package io.github.opensabe.spring.cloud.parent.common.config;

import io.github.opensabe.spring.cloud.parent.common.preheating.DelayApplicationReadyEventListener;
import io.github.opensabe.spring.cloud.parent.common.preheating.PreheatingProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.EndpointExposure;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 预热配置
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PreheatingProperties.class)
public class PreheatingConfiguration {
    @Bean
    @ConditionalOnProperty(value = "preheating.enabled", matchIfMissing = false, havingValue = "true")
    //必须通过 http 暴露 health 端口才启用
    @ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class, exposure = EndpointExposure.WEB)
    public DelayApplicationReadyEventListener delayApplicationReadyEventListener() {
        return new DelayApplicationReadyEventListener();
    }
}
