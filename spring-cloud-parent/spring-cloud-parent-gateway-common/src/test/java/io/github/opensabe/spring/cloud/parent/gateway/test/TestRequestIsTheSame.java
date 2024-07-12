package io.github.opensabe.spring.cloud.parent.gateway.test;

import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.gateway.filter.CommonLogFilter;
import io.github.opensabe.spring.cloud.parent.gateway.filter.InstanceCircuitBreakerFilter;
import io.github.opensabe.spring.cloud.parent.gateway.filter.QueryNormalizationFilter;
import io.github.opensabe.spring.cloud.parent.gateway.filter.RecordServiceNameFilter;
import io.github.opensabe.spring.cloud.parent.gateway.filter.RetryGatewayFilter;
import io.github.opensabe.spring.cloud.parent.gateway.filter.TraceIdFilter;
import io.github.opensabe.spring.cloud.parent.gateway.filter.TracedReactiveLoadBalancerClientFilter;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureObservability
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.gateway.httpclient.connect-timeout=500",
                "spring.cloud.gateway.httpclient.response-timeout=2000",
                "spring.cloud.gateway.routes[0].id=testService",
                "spring.cloud.gateway.routes[0].uri=lb://testService",
                "spring.cloud.gateway.routes[0].predicates[0]=Path=/httpbin/**",
                "spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1",
                "resilience4j.circuitbreaker.configs.default.failureRateThreshold=50",
                "resilience4j.circuitbreaker.configs.default.slidingWindowType=TIME_BASED",
                "resilience4j.circuitbreaker.configs.default.slidingWindowSize=5",
                "resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls=3",
                "resilience4j.circuitbreaker.configs.default.recordExceptions=java.lang.Exception"
        },
        classes = TestRequestIsTheSame.MockConfig.class
)
public class TestRequestIsTheSame extends CommonMicroServiceTest {
    @SpringBootApplication
    static class MockConfig {
    }

    private final String serviceId = "testService";

    @SpyBean
    private LoadBalancerClientFactory loadBalancerClientFactory;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private CircuitBreakerExtractor circuitBreakerExtractor;
    @LocalServerPort
    protected int port = 0;

    @Autowired
    private CommonLogFilter commonLogFilter;
    @Autowired
    private InstanceCircuitBreakerFilter instanceCircuitBreakerFilter;
    @Autowired
    private QueryNormalizationFilter queryNormalizationFilter;
    @Autowired
    private RecordServiceNameFilter recordServiceNameFilter;
    @Autowired
    private RetryGatewayFilter retryGatewayFilter;
    @Autowired
    private TracedReactiveLoadBalancerClientFilter reactiveLoadBalancerClientFilter;
    @Autowired
    private TraceIdFilter traceIdFilter;

    @Autowired
    private WebTestClient webClient;

    private Integer idx = 0;
    private DefaultRequest[] requests;

    //不同的测试方法的类对象不是同一个对象，会重新生成，保证互相没有影响
    ServiceInstance zone1Instance1 = new DefaultServiceInstance("instance1", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));
    ServiceInstance zone1Instance2 = new DefaultServiceInstance("instance2", serviceId, CONNECT_TIMEOUT_HOST, CONNECT_TIMEOUT_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));

    TracedCircuitBreakerRoundRobinLoadBalancer loadBalancerClientFactoryInstance = spy(TracedCircuitBreakerRoundRobinLoadBalancer.class);
    ServiceInstanceListSupplier serviceInstanceListSupplier = spy(ServiceInstanceListSupplier.class);

    @BeforeEach
    void setup() {
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(30)).build();
        //初始化 loadBalancerClientFactoryInstance 负载均衡器
        loadBalancerClientFactoryInstance.setCircuitBreakerRegistry(circuitBreakerRegistry);
        loadBalancerClientFactoryInstance.setCircuitBreakerExtractor(circuitBreakerExtractor);
        loadBalancerClientFactoryInstance.setServiceInstanceListSupplier(serviceInstanceListSupplier);
        requests = new DefaultRequest[3];
    }

    @Test
    public void test() {
        when(loadBalancerClientFactory.getInstance(serviceId, ReactorServiceInstanceLoadBalancer.class)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance2)));

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());

        when(loadBalancerClientFactoryInstance.choose(any())).thenAnswer(req -> {
            updateRequest(req.getArgument(0));
            return serviceInstanceListSupplier.get().next().flatMap((serviceInstances) -> {
                ServiceInstance serviceInstance = (ServiceInstance) serviceInstances.get(1);
                return Mono.just(new DefaultResponse(serviceInstance));
            });
        });

        webClient.get().uri("/httpbin/status/500").exchange().expectStatus().is5xxServerError();
        //必须用 == 验证是同一个对象
        assertTrue(requests[0] == requests[1]);
        assertTrue(requests[2] == requests[1]);
    }

    private void updateRequest(DefaultRequest request) {
        System.out.println(request);
        requests[idx++] = request;
    }
}