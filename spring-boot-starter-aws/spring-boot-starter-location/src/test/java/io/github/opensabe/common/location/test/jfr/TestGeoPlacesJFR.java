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
package io.github.opensabe.common.location.test.jfr;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.moditect.jfrunit.JfrEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.opensabe.common.location.service.GeocodeService;
import io.github.opensabe.common.location.test.common.GeoPlacesBaseTest;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jdk.jfr.consumer.RecordedEvent;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.geoplaces.model.ReverseGeocodeResponse;

/**
 * @author changhongwei
 * @date 2025/1/22 14:25
 * @description:
 */
@Log4j2
@Execution(ExecutionMode.SAME_THREAD)
@Disabled
@AutoConfigureObservability
@DisplayName("地理位置JFR事件测试")
public class TestGeoPlacesJFR extends GeoPlacesBaseTest {
    private final String address = "Samuel Asabia House 35 Marina,Lagos,Nigeria";
    private final List<Double> position = List.of(11.196417, 5.605130);
    public JfrEvents jfrEvents = new JfrEvents();
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    @Autowired
    private GeocodeService geocodeService;

    //    @Test
    @DisplayName("测试获取坐标功能 - 验证JFR事件记录")
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
    @DisplayName("测试反向地理编码功能 - 验证JFR事件记录")
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
