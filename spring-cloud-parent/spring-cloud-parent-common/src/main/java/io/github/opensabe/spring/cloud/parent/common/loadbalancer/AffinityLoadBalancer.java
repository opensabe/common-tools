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

/**
 * 亲和性负载均衡策略。
 * <p>
 * 根据请求上下文中的 affinity key 对象，从候选实例中选定目标。
 */
public interface AffinityLoadBalancer {

    /**
     * 按 affinity 对象选择目标实例。
     *
     * @param serviceInstances 候选实例列表
     * @param o affinity key 对象
     * @return 选中的实例
     */
    ServiceInstance execute(List<ServiceInstance> serviceInstances, Object o);
}
