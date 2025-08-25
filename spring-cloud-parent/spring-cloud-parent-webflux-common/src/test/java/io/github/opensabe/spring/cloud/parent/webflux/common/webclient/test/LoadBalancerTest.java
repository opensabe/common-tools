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

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.observation.Observation;
import io.netty.handler.timeout.ReadTimeoutException;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@AutoConfigureObservability
@SpringBootTest(properties = {
        "webclient.jfr.enabled=false",
        "spring.server.jfr.enabled=false",
        "eureka.client.enabled=false",
        "webclient.configs.testService.baseUrl=http://testService",
        "webclient.configs.testService.serviceName=testService",
        "webclient.configs.testService.responseTimeout=1500ms",
        "webclient.configs.testService.retryablePaths[0]=/delay/3",
        "webclient.configs.testService.retryablePaths[1]=/status/400",
        "spring.cloud.loadbalancer.zone=zone1",
        "resilience4j.retry.configs.default.maxAttempts=3",
        "resilience4j.circuitbreaker.configs.default.failureRateThreshold=50",
        "resilience4j.circuitbreaker.configs.default.slidingWindowType=TIME_BASED",
        "resilience4j.circuitbreaker.configs.default.slidingWindowSize=5",
        //因为重试是 3 次，为了防止断路器打开影响测试，设置为正好比重试多一次的次数，防止触发
        //同时我们在测试的时候也需要手动清空断路器统计
        "resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls=4",
        "resilience4j.circuitbreaker.configs.default.recordExceptions=java.lang.Exception"
}, classes = LoadBalancerTest.MockConfig.class)
public class LoadBalancerTest extends CommonMicroServiceTest {
    @SpringBootApplication
    static class MockConfig {
    }

    private final String serviceId = "testService";
    @Autowired
    private WebClientNamedContextFactory webClientNamedContextFactory;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private CircuitBreakerExtractor circuitBreakerExtractor;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    @MockitoSpyBean
    private LoadBalancerClientFactory loadBalancerClientFactory;
    private TracedCircuitBreakerRoundRobinLoadBalancer loadBalancerClientFactoryInstance = spy(TracedCircuitBreakerRoundRobinLoadBalancer.class);
    private ServiceInstanceListSupplier serviceInstanceListSupplier = spy(ServiceInstanceListSupplier.class);

    ServiceInstance zone1Instance1 = new DefaultServiceInstance("instance1", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));
    ServiceInstance zone1Instance2 = new DefaultServiceInstance("instance2", serviceId, CONNECT_TIMEOUT_HOST, CONNECT_TIMEOUT_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));
    ServiceInstance zone1Instance3 = new DefaultServiceInstance("instance3", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));

    @BeforeEach
    void setup() {
        //初始化 loadBalancerClientFactoryInstance 负载均衡器
        loadBalancerClientFactoryInstance.setCircuitBreakerRegistry(circuitBreakerRegistry);
        loadBalancerClientFactoryInstance.setCircuitBreakerExtractor(circuitBreakerExtractor);
        loadBalancerClientFactoryInstance.setServiceInstanceListSupplier(serviceInstanceListSupplier);
    }

    /**
     * 测试 断路器为微服务级别，且断路器打开后会重试
     */
    @Test
    public void testLevelOfCircuit() {
        when(loadBalancerClientFactory.getInstance(serviceId)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance3)));

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);

        //1.调用 /anyting为404，所以总共10+的调用导致熔断
        WebClient webClient = webClientNamedContextFactory.getWebClient(serviceId);
        for (int i = 0; i < 10; i++) {
            StepVerifier.create(webClient.get().uri("/anyting")
                            .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                    .expectErrorMatches(t -> t instanceof WebClientResponseException.NotFound || t instanceof CallNotPermittedException)
                    .verifyThenAssertThat();
        }

        reset(loadBalancerClientFactoryInstance);

        //2.熔断为微服务级别，导致/status/200熔断
        StepVerifier.create(webClient.get().uri("/status/200")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t instanceof CallNotPermittedException)
                .verifyThenAssertThat();
        verify(loadBalancerClientFactoryInstance, times(3)).choose(any());
    }

    /**
     * 测试connect time out ，无论是 GET请求还是post请求，都会重试
     */
    @Test
    public void testRetryOnConnectTimeout() {
        when(loadBalancerClientFactory.getInstance(serviceId)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance2)));

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);

        WebClient webClient = webClientNamedContextFactory.getWebClient(serviceId);
        Observation parent = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        parent.scoped(() -> {

            //1.测试 get 方法（默认 get 方法会重试）
            for (int i = 0; i < 10; i++) {
                try {
                    Mono<String> stringMono = webClient.get().uri("/anything").retrieve()
                            .bodyToMono(String.class);
                    System.out.println(stringMono.block());
                } catch (WebClientRequestException e) {
                    System.out.println("========" + e.getMessage());
                }
            }
            verify(loadBalancerClientFactoryInstance, atLeast(11)).choose(any());

            //2.测试 post 方法（默认 post 方法针对请求已经发出的不会重试，这里没有发出请求所以还是会重试的）
            circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
            reset(loadBalancerClientFactoryInstance);
            for (int i = 0; i < 10; i++) {
                try {
                    Mono<String> stringMono = webClient.post().uri("/anything").retrieve()
                            .bodyToMono(String.class);
                    System.out.println(stringMono.block());
                } catch (WebClientRequestException e) {
                    System.out.println("========" + e.getMessage());
                }
            }
            verify(loadBalancerClientFactoryInstance, atLeast(11)).choose(any());
        });
    }

    /**
     * 测试read time out 重试
     */
    @Test
    public void testRetryOnReadTimeout() {
        when(loadBalancerClientFactory.getInstance(serviceId)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance3)));

        WebClient webClient = webClientNamedContextFactory.getWebClient(serviceId);

        //1.配置1.5s超时，Get 延迟2s返回，则重试3次
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);
        StepVerifier.create(webClient.get().uri("/delay/2")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t.getCause() instanceof ReadTimeoutException)
                .verifyThenAssertThat();
        verify(loadBalancerClientFactoryInstance, times(3)).choose(any());

        //2.配置1.5s超时，post 延迟3s返回且路径在重试路径中，则重试3次
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);
        StepVerifier.create(webClient.post().uri("/delay/3")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t.getCause() instanceof ReadTimeoutException)
                .verifyThenAssertThat();
        verify(loadBalancerClientFactoryInstance, times(3)).choose(any());

        //3.配置1.5s超时，post延迟2s返回且路径不在重试路径中，则不重试
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);
        StepVerifier.create(webClient.post().uri("/delay/2")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t.getCause() instanceof ReadTimeoutException)
                .verifyThenAssertThat();
        verify(loadBalancerClientFactoryInstance, times(1)).choose(any());
    }

    /**
     * 测试 非200响应码
     */
    @Test
    public void testRetryOnResponceCode() {
        when(loadBalancerClientFactory.getInstance(serviceId)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance3)));

        WebClient webClient = webClientNamedContextFactory.getWebClient(serviceId);

        //1.返回 500,get请求，则重试3次
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);
        StepVerifier.create(webClient.get().uri("/status/500")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t instanceof WebClientResponseException.InternalServerError)
                .verifyThenAssertThat();
        verify(loadBalancerClientFactoryInstance, times(3)).choose(any());

        //2.返回 500,已发出去的post请求，则不重试
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);
        StepVerifier.create(webClient.post().uri("/status/500")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t instanceof WebClientResponseException.InternalServerError)
                .verifyThenAssertThat();

        verify(loadBalancerClientFactoryInstance, times(1)).choose(any());

        //3.返回 400,则不重试
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);
        StepVerifier.create(webClient.post().uri("/status/400")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t instanceof WebClientResponseException.BadRequest)
                .verifyThenAssertThat();
        verify(loadBalancerClientFactoryInstance, times(1)).choose(any());

    }

    /**
     * 测试针对 重试时 是同一个Request
     */
    @Test
    public void testRetrySameRequest() {
        when(loadBalancerClientFactory.getInstance(serviceId)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance3)));

        WebClient webClient = webClientNamedContextFactory.getWebClient(serviceId);

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());

        StepVerifier.create(webClient.get().uri("/status/500")
                        .retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode))
                .expectErrorMatches(t -> t instanceof WebClientResponseException.InternalServerError)
                .verifyThenAssertThat();
        Assertions.assertEquals(loadBalancerClientFactoryInstance.getRequestRequestDataContextMap().asMap().size(),1);
    }
}
