package io.github.opensabe.spring.cloud.parent.gateway.test;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.gateway.filter.AbstractTracedFilter;
import io.github.opensabe.spring.cloud.parent.gateway.filter.TraceIdFilter;
import io.github.opensabe.spring.cloud.parent.webflux.common.TracedPublisherFactory;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.TraceContext;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureObservability
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "spring.main.allow-circular-references=true",
                "eureka.client.enabled=false",
                "webclient.configs.testService.baseUrl=http://testService",
                "webclient.configs.testService.serviceName=testService",
                "webclient.configs.testService.responseTimeout=1500ms",
                "spring.cloud.loadbalancer.zone=zone1",
                "spring.cloud.gateway.server.webflux.httpclient.connect-timeout=500",
                "spring.cloud.gateway.server.webflux.httpclient.response-timeout=2000",
                "spring.cloud.gateway.server.webflux.routes[0].id=testService",
                "spring.cloud.gateway.server.webflux.routes[0].uri=lb://testService",
                "spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/httpbin/**",
                "spring.cloud.gateway.server.webflux.routes[0].filters[0]=StripPrefix=1",
                "resilience4j.circuitbreaker.configs.default.failureRateThreshold=50",
                "resilience4j.circuitbreaker.configs.default.slidingWindowType=COUNT_BASED",
                "resilience4j.circuitbreaker.configs.default.slidingWindowSize=5",
                "resilience4j.circuitbreaker.configs.default.minimumNumberOfCalls=3",
                "resilience4j.circuitbreaker.configs.default.recordExceptions=java.lang.Exception"
        },
        classes = TestGatewayObservation.MockConfig.class
)
public class TestGatewayObservation extends CommonMicroServiceTest {
    @SpringBootApplication
    public static class MockConfig {
        @Bean
        public WebClientFilter webClientFilter() {
            return new WebClientFilter();
        }
    }

    @Autowired
    private WebTestClient webClient;

    @MockitoSpyBean
    private LoadBalancerClientFactory loadBalancerClientFactory;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private CircuitBreakerExtractor circuitBreakerExtractor;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    private static final String serviceId = "testService";
    //不同的测试方法的类对象不是同一个对象，会重新生成，保证互相没有影响
    ServiceInstance zone1Instance1 = new DefaultServiceInstance("instance1", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));
    //not exists host, will cause connect time out
    ServiceInstance zone1Instance3 = new DefaultServiceInstance("instance3", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));

    TracedCircuitBreakerRoundRobinLoadBalancer loadBalancerClientFactoryInstance = spy(TracedCircuitBreakerRoundRobinLoadBalancer.class);
    ServiceInstanceListSupplier serviceInstanceListSupplier = spy(ServiceInstanceListSupplier.class);

    static class WebClientFilter extends AbstractTracedFilter implements InitializingBean {
        @Autowired
        private WebClientNamedContextFactory webClientNamedContextFactory;
        @Autowired
        private TracedPublisherFactory tracedPublisherFactory;
        private WebClient webClient;

        @Override
        public void afterPropertiesSet() {
            this.webClient = webClientNamedContextFactory.getWebClient(serviceId);
        }

        @Override
        protected Mono<Void> traced(ServerWebExchange exchange, GatewayFilterChain chain) {
            return tracedPublisherFactory.getTracedMono(webClient.get().uri("/anything")
                    .retrieve().bodyToMono(String.class)
                    .flatMap(s -> {
                        System.out.println("==================");
                        System.out.println(s);
                        System.out.println("==================");
                        return chain.filter(exchange);
                    }), TraceIdFilter.getObservation(exchange));
        }

        @Override
        protected int ordered() {
            return 0;
        }
    }

    @BeforeEach
    void setup() {
        //初始化 loadBalancerClientFactoryInstance 负载均衡器
        loadBalancerClientFactoryInstance.setCircuitBreakerRegistry(circuitBreakerRegistry);
        loadBalancerClientFactoryInstance.setCircuitBreakerExtractor(circuitBreakerExtractor);
        loadBalancerClientFactoryInstance.setServiceInstanceListSupplier(serviceInstanceListSupplier);
    }

    @Test
    public void testObservation() {
        //验证从 Spring Cloud Gateway 到后端服务的调用，是否会传递 traceId
        when(loadBalancerClientFactory.getInstance(serviceId, ReactorServiceInstanceLoadBalancer.class)).thenReturn(loadBalancerClientFactoryInstance);
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1, zone1Instance3)));
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        reset(loadBalancerClientFactoryInstance);
        webClient.get().uri("/httpbin/anything").exchange()
                .expectStatus().isEqualTo(200)
                .expectBody(HttpBinAnythingResponse.class).consumeWith(s -> {
                    System.out.println("=================");
                    System.out.println(s);
                    System.out.println("=================");
                    assertTrue(
                            s.getResponseBody().getHeaders().entrySet().stream()
                                    .anyMatch(entry -> {
                                        boolean b = StringUtils.equalsIgnoreCase(entry.getKey(), UnifiedObservationFactory.TRACE_PARENT);
                                        if (b) {
                                            System.out.println(entry);
                                            System.out.println("=================");
                                        }
                                        return b;
                                    })
                    );
                });

        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        //验证发出的请求 Trace 之后的效果，链路是追踪下来的
        var parent = Observation.start("parent", observationRegistry);
        parent.scoped(() -> {
            Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
            webClient.get().uri("/httpbin/anything")
                    //设置请求头，模拟上游服务发出的请求，带有 traceId 和 spanId
                    //因为 WebTestClient 不会自动塞入 traceId 和 spanId，所以需要手动设置
                    .header(UnifiedObservationFactory.TRACE_PARENT,
                            "00" + UnifiedObservationFactory.TRACEPARENT_DELIMITER +
                                    traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    + UnifiedObservationFactory.TRACEPARENT_DELIMITER + "00"
                    )
                    .exchange()
                    .expectStatus().isEqualTo(200)
                    .expectBody(HttpBinAnythingResponse.class).consumeWith(s -> {
                        System.out.println("=================");
                        System.out.println(s);
                        System.out.println("=================");
                        assertTrue(
                                s.getResponseBody().getHeaders().entrySet().stream()
                                        .anyMatch(entry -> {
                                            boolean b = StringUtils.equalsIgnoreCase(entry.getKey(), UnifiedObservationFactory.TRACE_PARENT);
                                            if (b) {
                                                System.out.println(entry);
                                                //发送的请求的 Header，traceId 是一样的，但是 spanId 是新的
                                                assertTrue(entry.getValue().get(0).contains(
                                                        traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER
                                                ));
                                                assertTrue(!entry.getValue().get(0).contains(
                                                        UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                                ));
                                            }
                                            return b;
                                        })
                        );
                    });
        });
    }
}
