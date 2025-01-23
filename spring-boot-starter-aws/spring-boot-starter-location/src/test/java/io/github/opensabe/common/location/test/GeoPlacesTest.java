package io.github.opensabe.common.location.test;

import io.github.opensabe.common.location.service.GeocodeService;
import io.github.opensabe.common.location.test.common.GeoPlacesBaseTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.geoplaces.model.ReverseGeocodeResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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