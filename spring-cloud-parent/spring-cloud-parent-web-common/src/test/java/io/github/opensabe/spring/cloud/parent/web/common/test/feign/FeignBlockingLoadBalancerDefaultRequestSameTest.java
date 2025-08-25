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
package io.github.opensabe.spring.cloud.parent.web.common.test.feign;

import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.web.common.test.CommonMicroServiceTest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@EnableFeignClients
@ActiveProfiles("feignDefaultRequest")
@SpringBootTest
@AutoConfigureObservability
public class FeignBlockingLoadBalancerDefaultRequestSameTest extends CommonMicroServiceTest {
    static final String TEST_SERVICE_1 = "testService1";
    static final String CONTEXT_ID_1 = "testService1Client";

    //参考 TestRequestIsTheSame  spock test
    @SpringBootApplication
    static class MockConfig {
    }

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private CircuitBreakerExtractor circuitBreakerExtractor;

    @Autowired
    private RetryRegistry retryRegistry;

    private DefaultRequest[] requests;
    private Integer idx = 0;

    //有意让resilience4j 重试  会调用FeignBlockingLoadBalancerClientExtend
    @FeignClient(name = TEST_SERVICE_1, contextId = CONTEXT_ID_1)
    static interface TestService1Client {
        @GetMapping("/status/500")
        String testGetRetryStatus500();
    }

    @Autowired
    TestService1Client testService1Client;

    @MockitoBean(name = "service1")
    ServiceInstance serviceInstance1;

    @MockitoSpyBean
    private LoadBalancerClientFactory loadBalancerClientFactory;

    @MockitoSpyBean
    DiscoveryClient discoveryClient;

    TracedCircuitBreakerRoundRobinLoadBalancer loadBalancerClientFactoryInstance = spy(TracedCircuitBreakerRoundRobinLoadBalancer.class);
    ServiceInstanceListSupplier serviceInstanceListSupplier = spy(ServiceInstanceListSupplier.class);

    @BeforeEach
    void setup() {
        when(serviceInstance1.getMetadata()).thenReturn(Map.ofEntries(Map.entry("zone", "zone1")));
        when(serviceInstance1.getInstanceId()).thenReturn("service6Instance");
        when(serviceInstance1.getHost()).thenReturn(GOOD_HOST);
        when(serviceInstance1.getPort()).thenReturn(GOOD_PORT);
        when(serviceInstance1.getScheme()).thenReturn("http");

        when(discoveryClient.getInstances(TEST_SERVICE_1)).thenReturn(List.of(serviceInstance1));

        loadBalancerClientFactoryInstance.setCircuitBreakerRegistry(circuitBreakerRegistry);
        loadBalancerClientFactoryInstance.setCircuitBreakerExtractor(circuitBreakerExtractor);
        loadBalancerClientFactoryInstance.setServiceInstanceListSupplier(serviceInstanceListSupplier);
        requests = new DefaultRequest[3];
    }

    /**
     * 目前已经在 FeignBlockingLoadBalancerClientExtend 保证了对于同一个请求，外层重试使用之前已创建的 DefaultRequest 而不是每次重新创建。
     * 测试需要在 TracedCircuitBreakerRoundRobinLoadBalancer getInstanceResponse 的方法入口参数对于重试验证 Request 是之前的
     * 测试默认次数默认为 3 次，方法为 get，遇到 internal server error 错误应该重试 3 次
     */
    @Test
    void testRetryByDefaultGetDefault() {
        //防止断路器影响
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        when(loadBalancerClientFactory.getInstance(TEST_SERVICE_1,
                ReactorServiceInstanceLoadBalancer.class)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(serviceInstance1)));

        //主要作用是知道  FeignBlockingLoadBalancerClientExtend 的 lbRequest 是 defaultRequest
        //
        //其中 ServiceInstance instance = loadBalancerClient.choose(serviceId, lbRequest) 中
        //loadBalancerClientFactory.getInstance(serviceId)
        //会 getInstance(serviceId, ReactorServiceInstanceLoadBalancer.class)
        //TracedCircuitBreakerRoundRobinLoadBalancer 模拟 choose 方法的 request
        when(loadBalancerClientFactoryInstance.choose(any(Request.class))).thenAnswer(req -> {
            updateRequest(req.getArgument(0));
            return serviceInstanceListSupplier.get().next().flatMap((serviceInstances) -> {
                ServiceInstance serviceInstance = (ServiceInstance) serviceInstances.get(0);
                return Mono.just(new DefaultResponse(serviceInstance));
            });
        });

        //测试默认遇到错误重试3次
        try {
            //会调用FeignBlockingLoadBalancerClientExtend的execute  Response execute(Request request, Request.Options options)
            testService1Client.testGetRetryStatus500();
        } catch (Exception exception) {
        }

        //当运行httpbin.org/status/500时候   插桩 TracedCircuitBreakerRoundRobinLoadBalancer 的choose方法获取其
        //getInstanceResponse 的方法入口参数对于重试验证 Request 是之前的FeignBlockingLoadBalancerClientExtend的同一个DefaultRequest
        //所以这里必须用 == 而不是 equals，因为我们要确保是同一个对象
        Assertions.assertTrue(requests[0] == requests[1]);
        Assertions.assertTrue(requests[1] == requests[2]);
    }

    private void updateRequest(DefaultRequest request) {
        System.out.println(request);
        requests[idx++] = request;
    }

}

