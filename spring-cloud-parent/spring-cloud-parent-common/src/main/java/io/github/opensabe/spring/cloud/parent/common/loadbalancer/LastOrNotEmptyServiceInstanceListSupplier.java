package io.github.opensabe.spring.cloud.parent.common.loadbalancer;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 如果为空集合，就返回上次的服务列表
 * 这个解决 eureka 重启丢失所有服务的问题
 */
@Log4j2
public class LastOrNotEmptyServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {
    private volatile List<ServiceInstance> lastServiceInstances = List.of();

    public LastOrNotEmptyServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
        super(delegate);
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return getDelegate().get().map(serviceInstances -> {
            if (CollectionUtils.isEmpty(serviceInstances)) {
                return lastServiceInstances;
            } else {
                lastServiceInstances = serviceInstances;
                return serviceInstances;
            }
        });
    }

    @Override
    public Flux<List<ServiceInstance>> get(Request request) {
        return getDelegate().get(request).map(serviceInstances -> {
            if (CollectionUtils.isEmpty(serviceInstances)) {
                return lastServiceInstances;
            } else {
                lastServiceInstances = serviceInstances;
                return serviceInstances;
            }
        });
    }
}
