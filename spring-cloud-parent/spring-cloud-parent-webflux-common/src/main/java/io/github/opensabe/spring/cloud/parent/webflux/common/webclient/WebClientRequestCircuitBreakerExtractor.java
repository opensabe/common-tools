package io.github.opensabe.spring.cloud.parent.webflux.common.webclient;

import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.Resilience4jUtil;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;

@Log4j2
public class WebClientRequestCircuitBreakerExtractor implements CircuitBreakerExtractor {
	@Override
	public CircuitBreaker getCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry, RequestDataContext context, String host, int port) {
		//这里 host 就是微服务名称，对于 webClient 使用微服务名称配置的 resilience4j 相关的元素
		String serviceName = context.getClientRequest().getUrl().getHost();
		String serviceInstanceMethodId = Resilience4jUtil.getServiceInstance(host, port);
		CircuitBreaker circuitBreaker;
		try {
			//每个服务实例具体方法一个resilience4j熔断记录器，在服务实例具体方法维度做熔断，所有这个服务的实例具体方法共享这个服务的resilience4j熔断配置
			circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId, serviceName);
		}
		catch (ConfigurationNotFoundException e) {
			circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId);
		}
		return circuitBreaker;
	}
}
