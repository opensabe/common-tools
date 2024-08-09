package io.github.opensabe.node.manager;


import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
