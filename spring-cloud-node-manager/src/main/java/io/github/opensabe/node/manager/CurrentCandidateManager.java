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
package io.github.opensabe.node.manager;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

/**
 * 基于服务发现实例排序的 Leader 选举辅助类。
 * <p>
 * 通过 {@link DiscoveryClient} 获取同服务全部实例，按给定比较器排序后判断当前实例是否为首位。
 */
@Log4j2
public class CurrentCandidateManager {

    /**
     * Spring Cloud 服务发现客户端。
     */
    private final DiscoveryClient discoveryClient;

    /**
     * 目标服务 ID。
     */
    private final String serviceId;

    /**
     * 当前实例 ID。
     */
    private final String instanceId;

    /**
     * @param discoveryClient 服务发现客户端
     * @param serviceId       服务 ID
     * @param instanceId      当前实例 ID
     */
    public CurrentCandidateManager(DiscoveryClient discoveryClient, String serviceId, String instanceId) {
        this.discoveryClient = discoveryClient;
        this.serviceId = serviceId;
        this.instanceId = instanceId;
    }

    /**
     * 在指定排序规则下，判断当前实例是否为排序后的第一个（Leader）。
     *
     * @param instanceComparator 实例比较器
     * @return 当前实例为 Leader 时返回 {@code true}
     */
    public boolean isLeader(Comparator<ServiceInstance> instanceComparator) {
        List<ServiceInstance> instances = this.discoveryClient.getInstances(serviceId);
        List<ServiceInstance> sorted = instances.stream().sorted(instanceComparator).collect(Collectors.toList());
        log.info("CurrentCandidateManager-isLeader: candidates: {}, sorted: {}, current instanceId: {}", JsonUtil.toJSONString(instances), JsonUtil.toJSONString(sorted), this.instanceId);
        if (CollectionUtils.isNotEmpty(sorted)) {
            return StringUtils.endsWithIgnoreCase(sorted.get(0).getInstanceId(), this.instanceId);
        } else {
            return false;
        }
    }
}
