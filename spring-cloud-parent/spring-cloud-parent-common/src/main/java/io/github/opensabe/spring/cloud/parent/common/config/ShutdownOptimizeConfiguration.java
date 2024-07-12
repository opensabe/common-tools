package io.github.opensabe.spring.cloud.parent.common.config;


import io.github.opensabe.spring.cloud.parent.common.shutdown.GracefulShutdownDelayBuffer;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration(proxyBeanMethods = false)
public class ShutdownOptimizeConfiguration {
    @Bean
    public GracefulShutdownDelayBuffer gracefulShutdownDelayBuffer() {
        return new GracefulShutdownDelayBuffer();
    }
}
