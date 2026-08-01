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
package io.github.opensabe.spring.cloud.parent.gateway.config;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.AdaptCachedBodyGlobalFilter;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Upgrade gate: CommonGatewayConfiguration enables request-body caching for every route
 * so retries can re-read Flux body.
 */
@DisplayName("CommonGatewayConfiguration body caching 契约")
class CommonGatewayConfigurationBodyCachingContractTest {

    @Test
    @DisplayName("init 时对每个 route 发送 EnableBodyCachingEvent")
    void enablesBodyCachingForEachRoute() {
        AdaptCachedBodyGlobalFilter filter = mock(AdaptCachedBodyGlobalFilter.class);
        GatewayProperties properties = mock(GatewayProperties.class);

        RouteDefinition route1 = new RouteDefinition();
        route1.setId("route-a");
        route1.setUri(URI.create("lb://svc-a"));

        RouteDefinition route2 = new RouteDefinition();
        route2.setId("route-b");
        route2.setUri(URI.create("lb://svc-b"));

        when(properties.getRoutes()).thenReturn(List.of(route1, route2));

        CommonGatewayConfiguration configuration = new CommonGatewayConfiguration();
        ReflectionTestUtils.setField(configuration, "adaptCachedBodyGlobalFilter", filter);
        ReflectionTestUtils.setField(configuration, "gatewayProperties", properties);

        configuration.init();

        ArgumentCaptor<EnableBodyCachingEvent> captor = ArgumentCaptor.forClass(EnableBodyCachingEvent.class);
        verify(filter, times(2)).onApplicationEvent(captor.capture());
        assertEquals(List.of("route-a", "route-b"),
                captor.getAllValues().stream().map(EnableBodyCachingEvent::getRouteId).toList());
    }
}
