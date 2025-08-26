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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JfrEventTest
@Log4j2
@SpringJUnitConfig
@AutoConfigureObservability
@SpringBootTest(
        classes = TestHttpServerRequestJFREvent.TestConfiguration.class,
        properties = {
                "eureka.client.enabled=false",
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
//jfr 测试需要串行，因为收集的是进程纬度的数据，如果并行会导致数据错乱
@Execution(ExecutionMode.SAME_THREAD)
//JFR 测试最好在本地做
@Disabled
public class TestHttpServerRequestJFREvent {
    public JfrEvents jfrEvents = new JfrEvents();
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Test
    public void test() {
        jfrEvents.reset();
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        observation.scoped(() -> {
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            webTestClient.post().uri("/test-normal-get/test-path-param?param1=paramValue1&param2=paramValue2")
                    .header("header1", "headerValue1")
                    .header("header2", "headerValue2")
                    //设置请求头，模拟上游服务发出的请求，带有 traceId 和 spanId
                    //因为 WebTestClient 不会自动塞入 traceId 和 spanId，所以需要手动设置
                    .header(UnifiedObservationFactory.TRACE_PARENT,
                            "00" + UnifiedObservationFactory.TRACEPARENT_DELIMITER +
                                    traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    + UnifiedObservationFactory.TRACEPARENT_DELIMITER + "00"
                    )
                    .bodyValue(Map.ofEntries(Map.entry("body1", "body1")))
                    .exchange()
                    .expectStatus().isOk();
            jfrEvents.awaitEvents();
            List<RecordedEvent> collect = jfrEvents.events()
                    .filter(e -> e.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerRequestJFREvent"))
                    .toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/test-normal-get/test-path-param");
            assertEquals(recordedEvent.getString("queryString"), "{param1=[paramValue1], param2=[paramValue2]}");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
        jfrEvents.reset();
        observation.scoped(() -> {
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            webTestClient.get().uri("/test-mono")
                    //设置请求头，模拟上游服务发出的请求，带有 traceId 和 spanId
                    //因为 WebTestClient 不会自动塞入 traceId 和 spanId，所以需要手动设置
                    .header(UnifiedObservationFactory.TRACE_PARENT,
                            "00" + UnifiedObservationFactory.TRACEPARENT_DELIMITER +
                                    traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    + UnifiedObservationFactory.TRACEPARENT_DELIMITER + "00"
                    )
                    .exchange()
                    .expectStatus().isOk();
            jfrEvents.awaitEvents();
            List<RecordedEvent> collect = jfrEvents.events()
                    .filter(e -> e.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerRequestJFREvent"))
                    .toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/test-mono");
            assertEquals(recordedEvent.getString("queryString"), "{}");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
        jfrEvents.reset();
        observation.scoped(() -> {
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            webTestClient.get().uri("/test-deferred")
                    //设置请求头，模拟上游服务发出的请求，带有 traceId 和 spanId
                    //因为 WebTestClient 不会自动塞入 traceId 和 spanId，所以需要手动设置
                    .header(UnifiedObservationFactory.TRACE_PARENT,
                            "00" + UnifiedObservationFactory.TRACEPARENT_DELIMITER +
                                    traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    + UnifiedObservationFactory.TRACEPARENT_DELIMITER + "00"
                    )
                    .exchange()
                    .expectStatus().isOk();
            jfrEvents.awaitEvents();
            List<RecordedEvent> collect = jfrEvents.events()
                    .filter(e -> e.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerRequestJFREvent"))
                    .toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/test-deferred");
            assertEquals(recordedEvent.getString("queryString"), "{}");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
        jfrEvents.reset();
        observation.scoped(() -> {
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            //对于抛出异常的，会被全局异常处理器 catch 并封装为 200 返回
            webTestClient.get().uri("/exception")
                    //设置请求头，模拟上游服务发出的请求，带有 traceId 和 spanId
                    //因为 WebTestClient 不会自动塞入 traceId 和 spanId，所以需要手动设置
                    .header(UnifiedObservationFactory.TRACE_PARENT,
                            "00" + UnifiedObservationFactory.TRACEPARENT_DELIMITER +
                                    traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    + UnifiedObservationFactory.TRACEPARENT_DELIMITER + "00"
                    )
                    .exchange()
                    .expectStatus().isOk();
            jfrEvents.awaitEvents();
            List<RecordedEvent> collect = jfrEvents.events()
                    .filter(e -> e.getEventType().getName().equals("io.github.opensabe.spring.cloud.parent.webflux.common.jfr.HttpServerRequestJFREvent"))
                    .toList();
            assertEquals(collect.size(), 1);
            RecordedEvent recordedEvent = collect.get(0);
            assertFalse(recordedEvent.getDuration().isZero());
            assertNotNull(recordedEvent.getString("requestHeaders"));
            assertEquals(recordedEvent.getString("uri"), "/exception");
            assertEquals(recordedEvent.getString("queryString"), "{}");
            assertEquals(recordedEvent.getString("traceId"), traceContext.traceId());
            assertNotEquals(recordedEvent.getString("spanId"), traceContext.spanId());
            assertEquals(recordedEvent.getInt("status"), 200);
            assertNotNull(recordedEvent.getString("responseHeaders"));
        });
    }

    @SpringBootApplication
    static class TestConfiguration {
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
}
