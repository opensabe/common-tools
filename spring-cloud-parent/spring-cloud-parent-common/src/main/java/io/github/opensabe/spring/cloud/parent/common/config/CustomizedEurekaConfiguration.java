package io.github.opensabe.spring.cloud.parent.common.config;

import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanAddNodeInfoCustomizer;
import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanCustomizer;
import io.github.opensabe.spring.cloud.parent.common.eureka.EurekaInstanceConfigBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
public class CustomizedEurekaConfiguration {
    @Bean
    public EurekaInstanceConfigBeanAddNodeInfoCustomizer eurekaInstanceConfigBeanAddNodeInfoCustomizer() {
        return new EurekaInstanceConfigBeanAddNodeInfoCustomizer();
    }

    @Bean
    public EurekaInstanceConfigBeanPostProcessor eurekaInstanceConfigBeanPostProcessor(
            List<EurekaInstanceConfigBeanCustomizer> eurekaInstanceConfigBeanCustomizers
    ) {
        return new EurekaInstanceConfigBeanPostProcessor(eurekaInstanceConfigBeanCustomizers);
    }
}
