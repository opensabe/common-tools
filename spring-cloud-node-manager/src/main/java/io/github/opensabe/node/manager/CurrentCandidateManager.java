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
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CurrentCandidateManager {
    private final DiscoveryClient discoveryClient;
    private final String serviceId;
    private final String instanceId;

    public CurrentCandidateManager(DiscoveryClient discoveryClient, String serviceId, String instanceId) {
        this.discoveryClient = discoveryClient;
        this.serviceId = serviceId;
        this.instanceId = instanceId;
    }

    /**
     * 判断在某种排序算法下，是否是第一个
     *
     * @param instanceComparator
     * @return
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
