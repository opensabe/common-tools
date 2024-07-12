package io.github.opensabe.spring.cloud.parent.gateway.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ComponentScan({"io.github.opensabe.spring.cloud.parent.gateway.filter"})
public class DefaultFilterConfiguration {
}
