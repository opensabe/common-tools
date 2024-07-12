package io.github.opensabe.spring.cloud.parent.common.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * 根据对象，决定负载均衡到哪个
 */
public interface AffinityLoadBalancer {

    ServiceInstance execute(List<ServiceInstance> serviceInstances, Object o);
}
