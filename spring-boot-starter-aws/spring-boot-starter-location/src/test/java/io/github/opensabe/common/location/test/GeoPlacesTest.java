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
package io.github.opensabe.common.location.test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.opensabe.common.location.service.GeocodeService;
import io.github.opensabe.common.location.test.common.GeoPlacesBaseTest;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.geoplaces.model.ReverseGeocodeResponse;

/**
 * @author changhongwei
 * @date 2024/11/27 19:52
 * @description: 地址测试
 */
@Log4j2
public class GeoPlacesTest extends GeoPlacesBaseTest {

    private final String address = "Samuel Asabia House 35 Marina,Lagos,Nigeria";

    private final List<Double> position = List.of(11.196417, 5.605130);

    @Autowired
    private GeocodeService geocodeService;

    //    @Test
    public void testGetCoordinates() {
        List<Double> coordinates = geocodeService.getCoordinates(address);
        assertNotNull(coordinates, "Coordinates should not be null");
        log.info("Coordinates: {}", coordinates);
    }

    //    @Test
    public void reverseGeocode() {
        ReverseGeocodeResponse reverseGeocodeResponse = geocodeService.reverseGeocode(position);
        log.info("Reverse Geocode Response: {}", reverseGeocodeResponse);
    }

}