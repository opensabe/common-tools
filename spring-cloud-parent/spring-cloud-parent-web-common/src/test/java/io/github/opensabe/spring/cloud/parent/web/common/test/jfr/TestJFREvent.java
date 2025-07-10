package io.github.opensabe.spring.cloud.parent.web.common.test.jfr;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.web.common.test.CommonMicroServiceTest;
import io.github.opensabe.spring.cloud.parent.web.common.test.feign.HttpBinAnythingResponse;
import feign.RetryableException;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Log4j2
@JfrEventTest
@SpringJUnitConfig
@SpringBootTest(
        classes = TestJFREvent.MockConfig.class,
        properties = {
                "management.tracing.sampling.probability=1.0",
                "eureka.client.enabled=false",
                "management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("jfr")
@AutoConfigureObservability
@EnableFeignClients
//JFR 测试最好在本地做
@Disabled
public class TestJFREvent extends CommonMicroServiceTest {
    @SpringBootApplication
    static class MockConfig {
        @Bean
        public TestService testService() {
            return new TestService();
        }
    }

    @RestController
    static class TestService {
        @PostMapping("/test-normal-get/{pathParam1}")
        public String testNormalGet(
                @PathVariable String pathParam1,
                @RequestParam String param1,
                @RequestParam String param2,
                @RequestHeader String header1,
                @RequestHeader String header2,
                @RequestBody Map<String, String> map
        ) {
            log.info(
                    "testNormalGet, pathParam1={}, param1={}, param2={}, header1={}, header2={}, map={}",
                    pathParam1, param1, param2, header1, header2, map
            );
            return Thread.currentThread().getName();
        }

        @GetMapping("/test-mono")
        public Mono<String> testMono() {
            Mono<String> result = Mono.delay(Duration.ofSeconds(1))
                    .flatMap(l -> {
                        log.info("sleep {} complete", l);
                        return Mono.just(Thread.currentThread().getName());
                    });
            log.info("method returned");
            return result;
        }

        @GetMapping("/test-deferred")
        public DeferredResult<String> testDeferred() {
            DeferredResult<String> result = new DeferredResult<>();
            CompletableFuture.runAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("sleep complete");
                result.setResult(Thread.currentThread().getName());
            });
            log.info("method returned");
            return result;
        }

        @GetMapping("/exception")
        public String exception() {
            throw new RuntimeException();
        }
    }

    public JfrEvents jfrEvents = new JfrEvents();

    @Autowired
    private TestService1Client testService1Client;

    @Test
    public void testHttpServerRequestJFREvent() {
        jfrEvents.reset();
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        observation.scoped(() -> {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("header1", "v1");
//            httpHeaders.add("header2", "v2");
            HttpEntity<Map<String, String>> request = new HttpEntity<>(Map.of(
                    "key1", "value1"
            ), httpHeaders);
            testRestTemplate.exchange(
                    "/test-normal-get/test-path-param?param1={param1}&param2={param2}",
                    HttpMethod.POST,
                    request,
                    Map.class,
                    "paramValue1", "paramValue2"
            );
            jfrEvents.awaitEvents();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            List<RecordedEvent> collect = jfrEvents.events().filter(recordedEvent -> {
                return recordedEvent.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent")
                        && recordedEvent.getString("traceId").equals(traceContext.traceId());
            }).toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertEquals(recordedEvent.getEventType().getName(), "io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent");
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/test-normal-get/test-path-param");
            assertEquals(recordedEvent.getString("queryString"), "param1=paramValue1&param2=paramValue2");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
        jfrEvents.reset();
        observation.scoped(() -> {
            String forObject = testRestTemplate.getForObject("/test-mono", String.class);
            jfrEvents.awaitEvents();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            List<RecordedEvent> collect = jfrEvents.events().filter(recordedEvent -> {
                return recordedEvent.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent")
                        && recordedEvent.getString("traceId").equals(traceContext.traceId());
            }).toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertEquals(recordedEvent.getEventType().getName(), "io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent");
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/test-mono");
            assertNull(recordedEvent.getString("queryString"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
        jfrEvents.reset();
        observation.scoped(() -> {
            String forObject = testRestTemplate.getForObject("/test-deferred", String.class);
            jfrEvents.awaitEvents();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            List<RecordedEvent> collect = jfrEvents.events().filter(recordedEvent -> {
                return recordedEvent.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent")
                        && recordedEvent.getString("traceId").equals(traceContext.traceId());
            }).toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertEquals(recordedEvent.getEventType().getName(), "io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent");
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/test-deferred");
            assertNull(recordedEvent.getString("queryString"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
        jfrEvents.reset();
        observation.scoped(() -> {
            //对于抛出异常的，会被全局异常处理器 catch 并封装为 200 返回
            Map forObject = testRestTemplate.getForObject("/exception", Map.class);
            jfrEvents.awaitEvents();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            List<RecordedEvent> collect = jfrEvents.events().filter(recordedEvent -> {
                return recordedEvent.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent")
                        && recordedEvent.getString("traceId").equals(traceContext.traceId());
            }).toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertEquals(recordedEvent.getEventType().getName(), "io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestJFREvent");
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/exception");
            assertNull(recordedEvent.getString("queryString"));
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
    }


    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    static final String TEST_SERVICE_1 = "TestOpenFeignJFREvent-TestService1";
    static final String CONTEXT_ID_1 = "TestOpenFeignJFREvent-testService1Client";

    @MockBean
    SimpleDiscoveryClient discoveryClient;
    List<ServiceInstance> normal = List.of(
            new DefaultServiceInstance(
                    TEST_SERVICE_1 + "_1", TEST_SERVICE_1,
                    GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1"))
            ));
    List<ServiceInstance> abnormal = List.of(
            new DefaultServiceInstance(
                    TEST_SERVICE_1 + "_2", TEST_SERVICE_1,
                    CONNECT_TIMEOUT_HOST, CONNECT_TIMEOUT_PORT, false, Map.ofEntries(Map.entry("zone", "zone1"))
            ));

    @FeignClient(name = TEST_SERVICE_1, contextId = CONTEXT_ID_1)
    interface TestService1Client {
        @GetMapping("/status/200")
        String getStatus200();

        @GetMapping("/status/500")
        String getStatus500();

        @PostMapping("/anything")
        HttpBinAnythingResponse postAnything(
                @RequestParam("param1") String param1,
                @RequestHeader("header1") String header1,
                @RequestBody Map<String, String> body
        );
    }

    @Test
    public void testNormal() {
        jfrEvents.reset();
        //正常情况
        when(discoveryClient.getInstances(TEST_SERVICE_1)).thenReturn(normal);

        Observation currentOrCreateEmptyObservation1 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentOrCreateEmptyObservation1.scoped(() -> {
            testService1Client.getStatus200();
        });
        Observation currentOrCreateEmptyObservation2= unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentOrCreateEmptyObservation2.scoped(() -> {
            Assertions.assertThrowsExactly(RetryableException.class, () -> {
                testService1Client.getStatus500();
            });
        });
        Observation currentOrCreateEmptyObservation3 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentOrCreateEmptyObservation3.scoped(() -> {
            testService1Client.postAnything(
                    "param1",
                    "header1",
                    Map.ofEntries(Map.entry("body1", "body1"))
            );
        });
        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events().filter(recordedEvent -> {
            return recordedEvent.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignRequestJFREvent")
                    && recordedEvent.getString("url").contains(TEST_SERVICE_1);
                }).collect(Collectors.toList());
        //1 + 3(重试) + 1
        assertEquals(5, events.size());
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentOrCreateEmptyObservation1);
        RecordedEvent recordedEvent = events.get(0);
        assertEquals(
                "io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignRequestJFREvent",
                recordedEvent.getEventType().getName()
        );
        assertEquals("GET", recordedEvent.getString("httpMethod"));
        assertEquals("http://TestOpenFeignJFREvent-TestService1/status/200", recordedEvent.getString("url"));
        assertEquals("{}", recordedEvent.getString("requestHeaders"));
        assertEquals(traceContext.traceId(), recordedEvent.getString("traceId"));
        assertNotEquals(traceContext.spanId(), recordedEvent.getString("spanId"));
        assertEquals(200, recordedEvent.getInt("status"));

        traceContext = UnifiedObservationFactory.getTraceContext(currentOrCreateEmptyObservation2);
        for (int i = 0; i < 3; i++) {
            recordedEvent = events.get(1 + i);
            assertEquals(
                    "io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignRequestJFREvent",
                    recordedEvent.getEventType().getName()
            );
            assertEquals("GET", recordedEvent.getString("httpMethod"));
            assertEquals("http://TestOpenFeignJFREvent-TestService1/status/500", recordedEvent.getString("url"));
            assertEquals("{}", recordedEvent.getString("requestHeaders"));
            assertEquals(traceContext.traceId(), recordedEvent.getString("traceId"));
            assertNotEquals(traceContext.spanId(), recordedEvent.getString("spanId"));
            assertEquals(500, recordedEvent.getInt("status"));
        }

        traceContext = UnifiedObservationFactory.getTraceContext(currentOrCreateEmptyObservation3);
        recordedEvent = events.get(4);
        assertEquals(
                "io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignRequestJFREvent",
                recordedEvent.getEventType().getName()
        );
        assertEquals("POST", recordedEvent.getString("httpMethod"));
        assertEquals("http://TestOpenFeignJFREvent-TestService1/anything?param1=param1", recordedEvent.getString("url"));
        assertEquals("{Content-Length=[17], Content-Type=[application/json], header1=[header1]}", recordedEvent.getString("requestHeaders"));
        assertEquals(traceContext.traceId(), recordedEvent.getString("traceId"));
        assertNotEquals(traceContext.spanId(), recordedEvent.getString("spanId"));
        assertEquals(200, recordedEvent.getInt("status"));
    }

    @Test
    public void testAbnormal() {
        jfrEvents.reset();
        //异常情况
        when(discoveryClient.getInstances(TEST_SERVICE_1)).thenReturn(abnormal);
        Observation currentOrCreateEmptyObservation1 = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        currentOrCreateEmptyObservation1.scoped(() -> {
            Assertions.assertThrowsExactly(RetryableException.class, () -> {
                testService1Client.postAnything(
                        "param1",
                        "header1",
                        Map.ofEntries(Map.entry("body1", "body1"))
                );
            });
        });
        //等待事件全部采集到
        jfrEvents.awaitEvents();
        List<RecordedEvent> events = jfrEvents.events()
                .filter(recordedEvent -> {
                    return recordedEvent.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignRequestJFREvent")
                            && recordedEvent.getString("url").contains(TEST_SERVICE_1);
                })
                .collect(Collectors.toList());

        assertEquals(3, events.size());
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentOrCreateEmptyObservation1);
        for (int i = 0; i < 3; i++) {
            RecordedEvent recordedEvent = events.get(i);
            assertEquals(
                    "io.github.opensabe.spring.cloud.parent.web.common.jfr.FeignRequestJFREvent",
                    recordedEvent.getEventType().getName()
            );
            assertEquals("POST", recordedEvent.getString("httpMethod"));
            assertEquals("http://TestOpenFeignJFREvent-TestService1/anything?param1=param1", recordedEvent.getString("url"));
            assertEquals("{Content-Length=[17], Content-Type=[application/json], header1=[header1]}", recordedEvent.getString("requestHeaders"));
            assertEquals(traceContext.traceId(), recordedEvent.getString("traceId"));
            assertNotEquals(traceContext.spanId(), recordedEvent.getString("spanId"));
            assertEquals(582, recordedEvent.getInt("status"));
            assertTrue(recordedEvent.getString("reason").contains("Connection refused"));
        }
    }
}
