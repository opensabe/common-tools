package io.github.opensabe.spring.cloud.starter.third.client.test;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.cloud.starter.third.client.webclient.ThirdPartyWebClientNamedContextFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

@JfrEventTest
@AutoConfigureObservability
@SpringBootTest(properties = {
        "management.tracing.sampling.probability=1",
        "third-party.webclient.configs.http-bin.base-url=http://httpbin.org"
})
public class TestThirdPartyWebClient {

    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }

    @Autowired
    private ThirdPartyWebClientNamedContextFactory thirdPartyWebClientNamedContextFactory;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    public JfrEvents jfrEvents = new JfrEvents();
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
                                System.out.println(entry);
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
                                    System.out.println(entry);
                                    //发送的请求的 Header，traceId 是一样的，但是 spanId 是新的
                                    assertTrue(entry.getValue().contains(
                                            traceContext.traceId() + UnifiedObservationFactory.TRACEPARENT_DELIMITER
                                    ));
                                    assertTrue(!entry.getValue().contains(
                                            UnifiedObservationFactory.TRACEPARENT_DELIMITER + traceContext.spanId()
                                    ));
                                }
                                return b;
                            })
            );
        });

    }
}
