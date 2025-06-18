package io.github.opensabe.spring.cloud.parent.webflux.common.webclient.test;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.common.loadbalancer.TracedCircuitBreakerRoundRobinLoadBalancer;
import io.github.opensabe.spring.cloud.parent.common.redislience4j.CircuitBreakerExtractor;
import io.github.opensabe.spring.cloud.parent.webflux.common.webclient.WebClientNamedContextFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(properties = {
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
}, classes = TestWebClientRequestJFREvent.MockConfig.class)
//jfr 测试需要串行，因为收集的是进程纬度的数据，如果并行会导致数据错乱
@Execution(ExecutionMode.SAME_THREAD)
//JFR 测试最好在本地做
@Disabled
public class TestWebClientRequestJFREvent extends CommonMicroServiceTest {
    @SpringBootApplication
    public static class MockConfig {
    }

    private final String serviceId = "testService";
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

    ServiceInstance zone1Instance1 = new DefaultServiceInstance("instance1", serviceId, GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1")));

    @BeforeEach
    void setup() {
        //初始化 loadBalancerClientFactoryInstance 负载均衡器
        loadBalancerClientFactoryInstance.setCircuitBreakerRegistry(circuitBreakerRegistry);
        loadBalancerClientFactoryInstance.setCircuitBreakerExtractor(circuitBreakerExtractor);
        loadBalancerClientFactoryInstance.setServiceInstanceListSupplier(serviceInstanceListSupplier);
    }

    public JfrEvents jfrEvents = new JfrEvents();

    /**
     * 测试 WebClient 正常调用
     */
    @Test
    public void testNormal() {
        jfrEvents.reset();
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        //初始化 loadBalancerClientFactoryInstance 负载均衡器
        when(loadBalancerClientFactory.getInstance(serviceId)).thenReturn(loadBalancerClientFactoryInstance);
        //初始化 serviceInstanceListSupplier 服务实例提供器返回都可以正常链接上的实例
        when(serviceInstanceListSupplier.get()).thenReturn(Flux.just(Lists.newArrayList(zone1Instance1)));
        //初始化 webClient
        WebClient webClient = webClientNamedContextFactory.getWebClient(serviceId);
        var currentObservation1 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentObservation1.scoped(() -> {
            Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/status/200").build())
                    .retrieve().bodyToMono(String.class).block();
            List<RecordedEvent> events = jfrEvents.events()
                    .filter(e -> e.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.webflux.common.jfr.WebClientRequestJFREvent"))
                    .toList();
            assertEquals(1, events.size());
            RecordedEvent recordedEvent = events.get(0);
            assertEquals("GET", recordedEvent.getString("httpMethod"));
            assertEquals("http://testService/status/200", recordedEvent.getString("url"));
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertNotNull(recordedEvent.getString("responseHeaders"));
            assertEquals(traceContext.traceId(), recordedEvent.getString("traceId"));
            assertNotEquals(traceContext.spanId(), recordedEvent.getString("spanId"));
            assertEquals(200, recordedEvent.getInt("status"));
        });
        jfrEvents.reset();
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        var currentObservation2 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentObservation2.scoped(() -> {
            Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
            assertThrows(WebClientResponseException.InternalServerError.class, () -> webClient.get()
                    .uri("/status/500")
                    .retrieve().bodyToMono(String.class).block()
            );
            List<RecordedEvent> events = jfrEvents.events()
                    .filter(e -> e.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.webflux.common.jfr.WebClientRequestJFREvent"))
                    .toList();
            assertEquals(1, events.size());
            RecordedEvent recordedEvent = events.get(0);
            assertEquals("GET", recordedEvent.getString("httpMethod"));
            assertEquals("http://testService/status/500", recordedEvent.getString("url"));
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertNotNull(recordedEvent.getString("responseHeaders"));
            assertEquals(traceContext.traceId(), recordedEvent.getString("traceId"));
            assertNotEquals(traceContext.spanId(), recordedEvent.getString("spanId"));
            assertEquals(500, recordedEvent.getInt("status"));
        });
        jfrEvents.reset();
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(c -> c.reset());
        var currentObservation3 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentObservation3.scoped(() -> {
            Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
            HttpBinAnythingResponse responseInParent = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/anything").queryParam("param1", "paramValue1").build())
                    .header("header1", "headerValue1")
                    .retrieve().bodyToMono(HttpBinAnythingResponse.class).block();
            List<RecordedEvent> events = jfrEvents.events()
                    .filter(e -> e.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.webflux.common.jfr.WebClientRequestJFREvent"))
                    .toList();
            assertEquals(1, events.size());
            RecordedEvent recordedEvent = events.get(0);
            assertEquals("POST", recordedEvent.getString("httpMethod"));
            assertEquals("http://testService/anything?param1=paramValue1", recordedEvent.getString("url"));
            assertTrue(recordedEvent.getString("requestHeaders").contains("header1:\"headerValue1\""));
            assertNotNull(recordedEvent.getString("responseHeaders"));
            assertEquals(traceContext.traceId(), recordedEvent.getString("traceId"));
            assertNotEquals(traceContext.spanId(), recordedEvent.getString("spanId"));
            assertEquals(200, recordedEvent.getInt("status"));
        });
    }
}
