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
package io.github.opensabe.node.manager.config;

import io.github.opensabe.node.manager.CurrentCandidateManager;
import io.github.opensabe.node.manager.NodeInfoActuator;
import io.github.opensabe.node.manager.NodeManager;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
public class NodeManagerConfiguration {
	@Bean
	public NodeManager getNodeManager(
            RedissonClient redissonClient,
			StringRedisTemplate redisTemplate,
			@Value("${spring.application.name}") String serviceId,
			@Value("${eureka.instance.instance-id}") String instanceId
	) {
		return new NodeManager(redissonClient, redisTemplate, serviceId, instanceId);
	}

	@Bean
	public NodeInfoActuator getNodeInfoActuator(NodeManager nodeManager) {
		return new NodeInfoActuator(nodeManager);
	}

	@Bean
    public CurrentCandidateManager getInstanceCandidateManager(
            DiscoveryClient discoveryClient,
            @Value("${spring.application.name}") String serviceId,
            @Value("${eureka.instance.instance-id}") String instanceId
    ) {
	    return new CurrentCandidateManager(discoveryClient, serviceId, instanceId);
    }
}
