package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;

public interface CircuitBreakerExtractor<T> {
	/**
	 * 通过负载均衡请求，以及实例信息，获取对应的 CircuitBreaker
	 * @param circuitBreakerRegistry
	 * @param requestDataContext
	 * @param host
	 * @param port
	 * @return
	 */
	CircuitBreaker getCircuitBreaker(
			CircuitBreakerRegistry circuitBreakerRegistry,
			RequestDataContext requestDataContext,
			String host,
			int port
	);
}

