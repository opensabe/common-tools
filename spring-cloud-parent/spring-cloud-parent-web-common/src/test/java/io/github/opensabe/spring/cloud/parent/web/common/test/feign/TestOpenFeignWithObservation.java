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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.parent.web.common.test.CommonMicroServiceTest;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * OpenFeign Micrometer Observation 测试：Traceparent 向下游传播且 child span 独立。
 */
@AutoConfigureTracing
@SpringBootTest(properties = {
        "management.tracing.sampling.probability=1.0",
        "spring.cloud.openfeign.micrometer.enabled=true"
})
@ActiveProfiles("observation")
@EnableFeignClients
@DisplayName("OpenFeign Observation 传播测试")
public class TestOpenFeignWithObservation extends CommonMicroServiceTest {
    static final String TEST_SERVICE_1 = "TestOpenFeignWithObservation-TestService1";
    static final String CONTEXT_ID_1 = "TestOpenFeignWithObservation-testService1Client";
    @MockitoBean
    SimpleDiscoveryClient discoveryClient;
    List<ServiceInstance> serviceInstances = List.of(
            new DefaultServiceInstance(
                    TEST_SERVICE_1 + "_1", TEST_SERVICE_1,
                    GOOD_HOST, GOOD_PORT, false, Map.ofEntries(Map.entry("zone", "zone1"))
            ));
    @Autowired
    private TestService1Client testService1Client;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    @Autowired
    private Tracer tracer;

    @BeforeEach
    void setup() {
        when(discoveryClient.getInstances(TEST_SERVICE_1)).thenReturn(serviceInstances);
    }

    /**
     * 活跃 Observation 下 Feign 请求须携带 Traceparent，traceId 相同、spanId 不同。
     */
    @Test
    @DisplayName("Feign 请求携带 Traceparent 且 child span 与 parent 区分")
    public void testRequestHasObservation() {
        assertNotNull(tracer, "Tracer bean required for W3C Traceparent injection");
        assertTrue(tracer != Tracer.NOOP, "Tracer must not be NOOP, got " + tracer.getClass());
        ObservationRegistry observationRegistry = unifiedObservationFactory.getObservationRegistry();
        var parent = Observation.start("parent", observationRegistry);
        parent.scoped(() -> {
            Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
            assertNotNull(traceContext, "active Observation must produce a TraceContext");
            assertTrue(StringUtils.isNotBlank(traceContext.traceId()), "traceId blank; tracer=" + tracer.getClass());
            var responseInParent = testService1Client.anything();
            assertNotNull(responseInParent);
            assertNotNull(responseInParent.getHeaders(),
                    "httpbin headers missing; body=" + responseInParent);
            assertTrue(
                    responseInParent.getHeaders().entrySet().stream()
                            .anyMatch(entry -> {
                                boolean b = StringUtils.equalsIgnoreCase(entry.getKey(), UnifiedObservationFactory.TRACE_PARENT);
                                if (b) {
                                    //发送的请求的 Header，traceId 是一样的，但是 spanId 是新的
                                    assertTrue(entry.getValue().get(0).contains(
                                            traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER
                                    ));
                                    assertTrue(!entry.getValue().get(0).contains(
                                            UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    ));
                                }
                                return b;
                            }),
                    () -> "expected Traceparent header, got: " + responseInParent.getHeaders()
                            + "; tracer=" + tracer.getClass()
                            + "; parentTrace=" + traceContext.traceId()
            );
        });
    }

    @FeignClient(name = TEST_SERVICE_1, contextId = CONTEXT_ID_1)
    interface TestService1Client {
        //向这个 http://httpbin.org/anything 发送的请求的响应体中包含了请求的所有信息
        @GetMapping("/anything")
        HttpBinAnythingResponse anything();
    }

    @SpringBootApplication
    static class MockConfig {
    }

}
