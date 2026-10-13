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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 控制是否在负载均衡中启用「按 K8S AZ（metadata {@code az-info}）+ {@link AzBalanceUtils} + Gumbel-max」路径。
 * 开启后由 {@link K8sAzGumbelLoadBalancerChooser} 参与选路；关闭则 {@link TracedCircuitBreakerRoundRobinLoadBalancer} 仅走原有逻辑。
 */
@Data
@ConfigurationProperties(prefix = "opensabe.loadbalancer.k8s-az-balance")
public class LoadBalancerK8sAzBalanceProperties {

    /**
     * 默认 false：与引入本配置前的 {@link TracedCircuitBreakerRoundRobinLoadBalancer} 行为一致。
     */
    private boolean enabled = false;
}
