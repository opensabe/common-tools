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
package io.github.opensabe.spring.cloud.parent.webflux.common.webclient.test;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClient;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.TraceContext;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@AutoConfigureObservability
@SpringBootTest(properties = {
        "webclient.jfr.enabled=false",
        "spring.server.jfr.enabled=false",
        "eureka.client.enabled=false",
        "webclient.configs.testService.baseUrl=http://testService",
        "webclient.configs.testService.serviceName=testService",
        "webclient.configs.testService.responseTimeout=1500ms",
        "spring.cloud.loadbalancer.zone=zone1",
        "resilience4j.retry.configs.default.maxAttempts=3",
        "resilience4j.circuitbreaker.configs.default.failureRateThreshold=50",
        "resilience4j.circuitbreaker.configs.default.slidingWindowType=TIME_BASED",
        "resilience4j.circuitbreaker.configs.default.slidingWindowSize=5",
        "resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls=4",
        "resilience4j.circuitbreaker.configs.default.recordExceptions=java.lang.Exception"
}, classes = TestWebFluxObservation.MockConfig.class)
public class TestWebFluxObservation extends CommonMicroServiceTest {
    private final String serviceId = "testService";
    ServiceInstance zone1Instance1 = new DefaultServiceInstance("instance1", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));
    ServiceInstance zone1Instance3 = new DefaultServiceInstance("instance3", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));
    @Autowired
    private WebClientNamedContextFactory webClientNamedContextFactory;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private CircuitBreakerExtractor circuitBreakerExtractor;
    @MockitoSpyBean
    private LoadBalancerClientFactory loadBalancerClientFactory;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    private TracedCircuitBreakerRoundRobinLoadBalancer loadBalancerClientFactoryInstance = spy(TracedCircuitBreakerRoundRobinLoadBalancer.class);
    private ServiceInstanceListSupplier serviceInstanceListSupplier = spy(ServiceInstanceListSupplier.class);

    @BeforeEach
    void setup() {
        //初始化 loadBalancerClientFactoryInstance 负载均衡器
        loadBalancerClientFactoryInstance.setCircuitBreakerRegistry(circuitBreakerRegistry);
        loadBalancerClientFactoryInstance.setCircuitBreakerExtractor(circuitBreakerExtractor);
        loadBalancerClientFactoryInstance.setServiceInstanceListSupplier(serviceInstanceListSupplier);
    }

    /**
     * 测试 WebClient 调用有链路信息
     */
    @Test
    public void testObservation() {
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        //初始化 loadBalancerClientFactoryInstance 负载均衡器
        when(loadBalancerClientFactory.getInstance(serviceId)).thenReturn(loadBalancerClientFactoryInstance);
        //初始化 serviceInstanceListSupplier 服务实例提供器返回都可以正常链接上的实例，并且是两个不同实例
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance3)));
        //重置断路器
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        //重置统计
        reset(loadBalancerClientFactoryInstance);
        //初始化 webClient
        WebClient webClient = webClientNamedContextFactory.getWebClient(serviceId);
        HttpBinAnythingResponse response = webClient.get().uri("/anything").retrieve().bodyToMono(HttpBinAnythingResponse.class).block();
        //验证发出的请求，即使没有被 Trace，也会在请求 Header 中包含 traceparent
        assertTrue(
                response.getHeaders().entrySet().stream()
                        .anyMatch(entry -> {
                            boolean b = StringUtils.equalsIgnoreCase(entry.getKey(), UnifiedObservationFactory.TRACE_PARENT);
                            if (b) {
                                System.out.println(entry);
                            }
                            return b;
                        })
        );
        //验证发出的请求 Trace 之后的效果，链路是追踪下来的
        var parent = Observation.start("parent", observationRegistry);
        parent.scoped(() -> {
            Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
            System.out.println(traceContext.traceId());
            System.out.println(UnifiedObservationFactory.getTraceContext(parent).traceId());
            HttpBinAnythingResponse responseInParent = webClient.get().uri("/anything").retrieve().bodyToMono(HttpBinAnythingResponse.class).block();
            assertTrue(
                    responseInParent.getHeaders().entrySet().stream()
                            .anyMatch(entry -> {
                                boolean b = StringUtils.equalsIgnoreCase(entry.getKey(), UnifiedObservationFactory.TRACE_PARENT);
                                if (b) {
                                    System.out.println(entry);
                                    //发送的请求的 Header，traceId 是一样的，但是 spanId 是新的
                                    assertTrue(entry.getValue().get(0).contains(
                                            traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER
                                    ));
                                    assertFalse(entry.getValue().get(0).contains(
                                            UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    ));
                                }
                                return b;
                            })
            );
        });
    }

    @SpringBootApplication
    public static class MockConfig {
    }
}
