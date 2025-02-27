package io.github.opensabe.common.location.test.jfr;

import io.github.opensabe.common.location.jfr.LocationJFREvent;
import io.github.opensabe.common.location.observation.LocationContext;
import io.github.opensabe.common.location.service.GeocodeService;
import io.github.opensabe.common.location.test.common.GeoPlacesBaseTest;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.testcontainers.shaded.org.yaml.snakeyaml.util.ArrayUtils;
import software.amazon.awssdk.services.geoplaces.model.ReverseGeocodeResponse;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author changhongwei
 * @date 2025/1/22 14:25
 * @description:
 */
@Log4j2
@Execution(ExecutionMode.SAME_THREAD)
@Disabled
@AutoConfigureObservability
public class TestGeoPlacesJFR extends GeoPlacesBaseTest {
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    @Autowired
    private GeocodeService geocodeService;

    public JfrEvents jfrEvents = new JfrEvents();

    private final String address = "Samuel Asabia House 35 Marina,Lagos,Nigeria";

    private final List<Double> position = List.of(11.196417, 5.605130);
//    @Test
    public void testGetCoordinates() {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        observation.scoped(() -> {
            List<Double> coordinates = geocodeService.getCoordinates(address);
            assertNotNull(coordinates, "Coordinates should not be null");
        });

        // 等待 JFR 事件生成
        jfrEvents.awaitEvents();

        // 验证 JFR 事件
        List<RecordedEvent> locationJFREvents = this.getLocationJFREvents("getCoordinates");
        assertFalse(locationJFREvents.isEmpty(), "JFR events for getCoordinates should not be empty");

        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        for (RecordedEvent event : locationJFREvents) {
            assertEquals("getCoordinates", event.getString("methodName"));
            assertEquals(address, event.getString("requestParams"));
            assertNotNull(event.getString("response"));
            assertTrue(event.getLong("executionTime") > 0);
            assertTrue(event.getBoolean("successful"));
            assertEquals(traceContext.traceId(), event.getString("traceId"));
            assertNotEquals(traceContext.spanId(), event.getString("spanId"));
        }
    }

//    @Test
    public void testReverseGeocode() {
        Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
        observation.scoped(() -> {
            ReverseGeocodeResponse reverseGeocodeResponse = geocodeService.reverseGeocode(position);
            assertNotNull(reverseGeocodeResponse, "Coordinates should not be null");
        });

        // 等待 JFR 事件生成
        jfrEvents.awaitEvents();

        // 验证 JFR 事件
        List<RecordedEvent> locationJFREvents = this.getLocationJFREvents("reverseGeocode");
        assertFalse(locationJFREvents.isEmpty(), "JFR events for reverseGeocode should not be empty");

        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        for (RecordedEvent event : locationJFREvents) {
            assertEquals("reverseGeocode", event.getString("methodName"));
            assertEquals(position.toString(), event.getString("requestParams"));
            assertNotNull(event.getString("response"));
            assertTrue(event.getLong("executionTime") > 0);
            assertTrue(event.getBoolean("successful"));
            assertEquals(traceContext.traceId(), event.getString("traceId"));
            assertNotEquals(traceContext.spanId(), event.getString("spanId"));
        }
    }

    private List<RecordedEvent> getLocationJFREvents(String methodName) {
        return jfrEvents.events()
                .filter(event -> event.getEventType().getName().equals("io.github.opensabe.common.location.jfr.LocationJFREvent"))
                .filter(event -> methodName.equals(event.getString("methodName")))
                .toList();
    }

}
