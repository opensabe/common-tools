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
package io.github.opensabe.spring.cloud.starter.third.client.test;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.starter.third.client.webclient.ThirdPartyWebClientNamedContextFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import lombok.extern.log4j.Log4j2;

@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(properties = {
        "management.tracing.sampling.probability=1",
})
@Log4j2
public class TestThirdPartyWebClient extends CommonMicroServiceTest {

    @Autowired
    private ThirdPartyWebClientNamedContextFactory thirdPartyWebClientNamedContextFactory;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("third-party.webclient.configs.http-bin.base-url", () -> "http://" + GOOD_HOST + ":" + GOOD_PORT);
    }

    /**
     * 测试发出请求 Header 中包含 Accept-Encoding: gzip
     */
    @Test
    public void testCompressed() {
        WebClient webClient = thirdPartyWebClientNamedContextFactory.getWebClient("http-bin");
        ResponseEntity<HttpBinAnythingResponse> entity = webClient.get().uri("/anything").retrieve().toEntity(HttpBinAnythingResponse.class).block();
        String accept = entity.getBody().getHeaders().get("Accept-Encoding").toString();
        Assertions.assertTrue(accept.contains("gzip"));
    }

    @Test
    public void testObservation() {
        WebClient webClient = thirdPartyWebClientNamedContextFactory.getWebClient("http-bin");
        ResponseEntity<HttpBinAnythingResponse> entity = webClient.get().uri("/anything").retrieve().toEntity(HttpBinAnythingResponse.class).block();
        //验证发出的请求，即使没有被 Trace，也会在请求 Header 中包含 traceparent
        assertTrue(
                entity.getBody().getHeaders().entrySet().stream()
                        .anyMatch(entry -> {
                            boolean b = StringUtils.equalsIgnoreCase(entry.getKey(), UnifiedObservationFactory.TRACE_PARENT);
                            if (b) {
                                log.info("traceparent: {}", entry.getValue());
                            }
                            return b;
                        })
        );

        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        observation.scoped(() -> {
            ResponseEntity<HttpBinAnythingResponse> entityInObservation = webClient.get().uri("/anything").retrieve().toEntity(HttpBinAnythingResponse.class).block();
            Observation currentObservation = unifiedObservationFactory.getCurrentObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(currentObservation);
            assertTrue(
                    entityInObservation.getBody().getHeaders().entrySet().stream()
                            .anyMatch(entry -> {
                                boolean b = StringUtils.equalsIgnoreCase(entry.getKey(), UnifiedObservationFactory.TRACE_PARENT);
                                if (b) {
                                    log.info("traceparent: {}", entry.getValue());
                                    //发送的请求的 Header，traceId 是一样的，但是 spanId 是新的
                                    String collect = entry.getValue().stream().collect(Collectors.joining());
                                    assertTrue(collect.contains(
                                            traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER
                                    ));
                                    assertTrue(!collect.contains(
                                            UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    ));
                                }
                                return b;
                            })
            );
        });

    }

    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }
}
