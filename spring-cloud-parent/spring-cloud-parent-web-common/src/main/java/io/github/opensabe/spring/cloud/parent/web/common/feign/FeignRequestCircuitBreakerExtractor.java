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
package io.github.opensabe.spring.cloud.parent.web.common.feign;

import java.lang.reflect.Method;

import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClientExtend;

import feign.RequestTemplate;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.Resilience4jUtil;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FeignRequestCircuitBreakerExtractor implements CircuitBreakerExtractor {
    @Override
    public CircuitBreaker getCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry, RequestDataContext context, String host, int port) {
        RequestTemplate requestTemplate = (RequestTemplate) context.getClientRequest().getAttributes()
                .get(FeignBlockingLoadBalancerClientExtend.REQUEST_TEMPLATE);
        Method method = requestTemplate.methodMetadata().method();
        String serviceInstanceMethodId = Resilience4jUtil.getServiceInstanceMethodId(host, port, method);
        //目前这个改动主要用于从feignClient 的proxy实体中获取其contextId 而不是从FeignClient的接口中的declaredMethod中获取contextId，原因是
        //需要预热的FeignClient的extends的预热接口的方法没有@FeignClient 这样会报错annotation is null, contextId cannot been found 详情参考FeignPreheatingBase
        FeignClient annotation = requestTemplate.feignTarget().type().getAnnotation(FeignClient.class);
        //和 Retry 保持一致，使用 contextId，而不是微服务名称
        String contextId = annotation.contextId();
        CircuitBreaker circuitBreaker;
        try {
            //每个服务实例具体方法一个resilience4j熔断记录器，在服务实例具体方法维度做熔断，所有这个服务的实例具体方法共享这个服务的resilience4j熔断配置
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId, contextId);
        } catch (ConfigurationNotFoundException e) {
            circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceInstanceMethodId);
        }
        return circuitBreaker;
    }
}
