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

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.cloud.client.loadbalancer.RequestDataContext;

import com.google.common.collect.Sets;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 与一次 {@link RequestDataContext} 绑定的负载均衡过程状态（重试、已选实例等）。
 */
@Data
@NoArgsConstructor
public class LoadBalancerRequestTraceContext {

    /** 本 trace 内已选过的 host:port */
    private final Set<String> calledInstances = Sets.newHashSet();
    /** 本 trace 内已选过的 K8S 节点标识 */
    private final Set<String> calledNodes = Sets.newHashSet();
    /**
     * 约 5% 概率为 true，排序明细走 INFO，其余走 DEBUG，用于控制日志量。
     */
    private final boolean detailLog = ThreadLocalRandom.current().nextInt(0, 10000) < 500;
    /**
     * 第一次选择前为 0；每次 {@link TracedCircuitBreakerRoundRobinLoadBalancer} 记录选择后自增，大于 0 表示重试路径。
     */
    private int count = 0;
}
