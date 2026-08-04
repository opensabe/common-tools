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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.config.LoadBalancerZoneConfig;
import org.springframework.cloud.loadbalancer.core.DelegatingServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import reactor.core.publisher.Flux;

/**
 * 同 Zone 服务实例列表 Supplier。
 * <p>
 * 只返回与当前实例同一 Zone 的实例；无同 Zone 实例时返回空列表（禁止跨 Zone 调用）。
 */
public class SameZoneOnlyServiceInstanceListSupplier extends DelegatingServiceInstanceListSupplier {

    /** Eureka metadata 中的 zone 键名。 */
    private static final String ZONE = "zone";

    /** 当前实例所在 Zone 配置。 */
    private final LoadBalancerZoneConfig zoneConfig;

    /** 缓存的当前 Zone，懒加载。 */
    private String zone;

    /**
     * @param delegate 下游 Supplier
     * @param zoneConfig 本实例 Zone 配置
     */
    public SameZoneOnlyServiceInstanceListSupplier(ServiceInstanceListSupplier delegate,
                                                   LoadBalancerZoneConfig zoneConfig) {
        super(delegate);
        this.zoneConfig = zoneConfig;
    }

    /** {@inheritDoc}；结果经 {@link #filteredByZone(List)} 过滤。 */
    @Override
    public Flux<List<ServiceInstance>> get() {
        return getDelegate().get().map(this::filteredByZone);
    }

    /**
     * {@inheritDoc}；结果经 Zone 过滤。
     *
     * @param request 负载均衡请求
     * @return 同 Zone 实例列表 Flux
     */
    @Override
    public Flux<List<ServiceInstance>> get(Request request) {
        return getDelegate().get(request).map(this::filteredByZone);
    }

    /**
     * 按本实例 Zone 过滤；无同 Zone 实例时返回空列表。
     *
     * @param serviceInstances 原始实例列表
     * @return 过滤后的实例列表
     */
    private List<ServiceInstance> filteredByZone(List<ServiceInstance> serviceInstances) {
        if (zone == null) {
            zone = zoneConfig.getZone();
        }
        if (zone != null) {
            List<ServiceInstance> filteredInstances = new ArrayList<>();
            for (ServiceInstance serviceInstance : serviceInstances) {
                String instanceZone = getZone(serviceInstance);
                if (zone.equalsIgnoreCase(instanceZone)) {
                    filteredInstances.add(serviceInstance);
                }
            }
            if (filteredInstances.size() > 0) {
                return filteredInstances;
            }
        }
        /**
         * @see ZonePreferenceServiceInstanceListSupplier 在没有相同zone实例的时候返回的是所有实例
         * 我们这里为了实现不同 zone 之间不互相调用需要返回空列表
         */
        return List.of();
    }

    /**
     * 从实例 metadata 读取 zone。
     *
     * @param serviceInstance 服务实例
     * @return zone 名称，metadata 缺失时为 null
     */
    private String getZone(ServiceInstance serviceInstance) {
        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null) {
            return metadata.get(ZONE);
        }
        return null;
    }

}
