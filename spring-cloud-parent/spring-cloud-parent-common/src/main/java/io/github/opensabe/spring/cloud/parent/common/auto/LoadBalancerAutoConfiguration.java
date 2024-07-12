package io.github.opensabe.spring.cloud.parent.common.auto;

import io.github.opensabe.spring.cloud.parent.common.config.DefaultLoadBalancerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@AutoConfiguration
@LoadBalancerClients(defaultConfiguration = DefaultLoadBalancerConfiguration.class)
public class LoadBalancerAutoConfiguration {
}
