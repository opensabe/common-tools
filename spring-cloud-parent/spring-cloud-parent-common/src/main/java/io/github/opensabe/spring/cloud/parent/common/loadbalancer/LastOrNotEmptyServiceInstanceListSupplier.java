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
 * 非空兜底服务实例列表 Supplier。
 * <p>
 * 当下游返回空列表时回退到上次非空结果，缓解 Eureka 重启瞬间实例列表丢失。
 */
@Log4j2
public class LastOrNotEmptyServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {
    /** 最近一次非空实例列表缓存。 */
    private volatile List<ServiceInstance> lastServiceInstances = List.of();

    /**
     * @param delegate 下游 Supplier
     */
    public LastOrNotEmptyServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
        super(delegate);
    }

    /** {@inheritDoc}；空列表时回退至 {@link #lastServiceInstances}。 */
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

    /**
     * {@inheritDoc}；空列表时回退至缓存。
     *
     * @param request 负载均衡请求
     * @return 实例列表 Flux
     */
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
