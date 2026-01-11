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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AzBalanceUtils {
    /**
     * 计算负载均衡比例，优先满足同可用区调用
     * @param sourceserviceInstanceMap
     * @param targetServiceInstanceMap
     * @return
     */
    public static Map<String, Map<String, Integer>> getLoadBalancingRatio(
            Map<String, List> sourceserviceInstanceMap,
            Map<String, List> targetServiceInstanceMap
    ) {
        //假设每个实例请求数量为 100，统计总请求数量
        int eachInstanceRequestCount = 1000000;
        int totalRequestCount = 0;
        for (List instances : sourceserviceInstanceMap.values()) {
            totalRequestCount += instances.size() * eachInstanceRequestCount;
        }
        //计算 targetServiceInstanceMap 中每个实例的请求数量
        int countOfTargetInstances = 0;
        for (List instances : targetServiceInstanceMap.values()) {
            countOfTargetInstances += instances.size();
        }
        if (countOfTargetInstances == 0) {
            return new LinkedHashMap<>();
        }
        //不能整除也没事，尽量均摊，但需要处理余数
        int countOfEachTargetInstanceRequest = totalRequestCount / countOfTargetInstances;
        int remainder = totalRequestCount % countOfTargetInstances;
        //targetServiceInstanceMap 转化为 ServiceInstance 数量 * 每个请求数量之后放入 Map
        //将余数按可用区实例数比例分配，确保所有请求都被分配
        Map<String, Integer> targetAzRequestMap = new HashMap<>();
        int remainderDistributed = 0;
        for (Map.Entry<String, List> entry : targetServiceInstanceMap.entrySet()) {
            String azName = entry.getKey();
            List instances = entry.getValue();
            int instanceCount = instances.size();
            int baseRequestsForAz = instanceCount * countOfEachTargetInstanceRequest;
            //按实例数比例分配余数
            int remainderForAz = (int) ((double) instanceCount / countOfTargetInstances * remainder);
            remainderDistributed += remainderForAz;
            targetAzRequestMap.put(azName, baseRequestsForAz + remainderForAz);
        }
        //如果还有未分配的余数（由于整数除法舍入），分配给实例数最多的可用区
        if (remainderDistributed < remainder) {
            String maxInstanceAz = null;
            int maxInstanceCount = 0;
            for (Map.Entry<String, List> entry : targetServiceInstanceMap.entrySet()) {
                int instanceCount = entry.getValue().size();
                if (instanceCount > maxInstanceCount) {
                    maxInstanceCount = instanceCount;
                    maxInstanceAz = entry.getKey();
                }
            }
            if (maxInstanceAz != null) {
                targetAzRequestMap.put(maxInstanceAz, targetAzRequestMap.get(maxInstanceAz) + (remainder - remainderDistributed));
            }
        }

        //重新整流，保证同可用区调用：
        //遍历 sourceserviceInstanceMap 中的可用区，优先满足同可用区调用，在 targetAzRequestMap 同可用区中扣减请求数量
        //如果同可用区请求数量不够，则从其他可用区平均扣减
        //Map 结构是 AzName -> (AzName -> RequestCount)
        Map<String, Map<String, Integer>> finalAzRequestMap = new LinkedHashMap<>();
        for (Map.Entry<String, List> sourceEntry : sourceserviceInstanceMap.entrySet()) {
            String sourceAzName = sourceEntry.getKey();
            int sourceRequestCount = sourceEntry.getValue().size() * eachInstanceRequestCount;
            Map<String, Integer> azRequestMap = new LinkedHashMap<>();
            //优先满足同可用区调用
            if (targetAzRequestMap.containsKey(sourceAzName)) {
                int availableRequests = targetAzRequestMap.get(sourceAzName);
                if (availableRequests >= sourceRequestCount) {
                    azRequestMap.put(sourceAzName, sourceRequestCount);
                    targetAzRequestMap.put(sourceAzName, availableRequests - sourceRequestCount);
                } else {
                    azRequestMap.put(sourceAzName, availableRequests);
                    targetAzRequestMap.put(sourceAzName, 0);
                }
            }
            //如果同可用区请求数量不够，则从其他可用区按照剩余请求数量平均扣减
            int remainingRequests = sourceRequestCount - azRequestMap.getOrDefault(sourceAzName, 0);
            if (remainingRequests > 0) {
                //使用更精确的分配算法，确保所有请求都被分配
                //先计算总可用容量
                int totalAvailableRequests = 0;
                for (int requests : targetAzRequestMap.values()) {
                    totalAvailableRequests += requests;
                }
                
                if (totalAvailableRequests > 0) {
                    //收集所有可用的目标可用区及其容量
                    java.util.List<Map.Entry<String, Integer>> availableTargets = new java.util.ArrayList<>();
                    for (Map.Entry<String, Integer> targetEntry : targetAzRequestMap.entrySet()) {
                        String targetAzName = targetEntry.getKey();
                        int availableRequests = targetEntry.getValue();
                        if (availableRequests > 0 && !targetAzName.equals(sourceAzName)) {
                            availableTargets.add(targetEntry);
                        }
                    }
                    
                    if (!availableTargets.isEmpty()) {
                        //使用累加误差的方式，确保所有请求都被分配
                        int totalAllocated = 0;
                        int targetIndex = 0;
                        
                        for (Map.Entry<String, Integer> targetEntry : availableTargets) {
                            String targetAzName = targetEntry.getKey();
                            int availableRequests = targetEntry.getValue();
                            
                            //计算按比例分配的请求数量
                            //使用累加误差的方式，确保总和等于 remainingRequests
                            int allocatedRequests;
                            if (targetIndex == availableTargets.size() - 1) {
                                //最后一个可用区，补齐所有剩余请求
                                allocatedRequests = remainingRequests - totalAllocated;
                            } else {
                                //按比例分配，使用长整型避免溢出
                                long preciseAllocation = (long) availableRequests * remainingRequests / totalAvailableRequests;
                                allocatedRequests = (int) preciseAllocation;
                            }
                            
                            //确保不超过可用容量
                            if (allocatedRequests > availableRequests) {
                                allocatedRequests = availableRequests;
                            }
                            //确保不超过剩余请求
                            if (allocatedRequests > remainingRequests - totalAllocated) {
                                allocatedRequests = remainingRequests - totalAllocated;
                            }
                            //确保不为负数
                            if (allocatedRequests < 0) {
                                allocatedRequests = 0;
                            }
                            
                            if (allocatedRequests > 0) {
                                azRequestMap.put(targetAzName, azRequestMap.getOrDefault(targetAzName, 0) + allocatedRequests);
                                targetAzRequestMap.put(targetAzName, availableRequests - allocatedRequests);
                                totalAllocated += allocatedRequests;
                            }
                            
                            targetIndex++;
                            
                            //如果已经分配完所有请求，退出循环
                            if (totalAllocated >= remainingRequests) {
                                break;
                            }
                        }
                        
                        //如果还有未分配的请求（由于舍入误差），分配给第一个有容量的可用区
                        if (totalAllocated < remainingRequests) {
                            for (Map.Entry<String, Integer> targetEntry : availableTargets) {
                                String targetAzName = targetEntry.getKey();
                                //使用更新后的容量值
                                int availableRequests = targetAzRequestMap.get(targetAzName);
                                if (availableRequests > 0) {
                                    int additionalAllocation = Math.min(availableRequests, remainingRequests - totalAllocated);
                                    azRequestMap.put(targetAzName, azRequestMap.getOrDefault(targetAzName, 0) + additionalAllocation);
                                    targetAzRequestMap.put(targetAzName, availableRequests - additionalAllocation);
                                    totalAllocated += additionalAllocation;
                                    if (totalAllocated >= remainingRequests) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            finalAzRequestMap.put(sourceAzName, azRequestMap);
        }
        return finalAzRequestMap;
    }
}
