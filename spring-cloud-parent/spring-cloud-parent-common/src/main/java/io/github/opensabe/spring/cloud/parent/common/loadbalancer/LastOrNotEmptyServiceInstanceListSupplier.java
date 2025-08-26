/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.cloud.parent.common.loadbalancer;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.util.CollectionUtils;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

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
