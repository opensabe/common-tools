package io.github.opensabe.common.location.service;

import io.github.opensabe.common.location.vo.GeoLocationData;
import io.github.opensabe.common.testcontainers.integration.SingleValkeyIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, SingleValkeyIntegrationTest.class})
@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
public class LocationImplWithValkeyTest {

    @EnableAutoConfiguration
    @Configuration
    public static class App {
    }
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleValkeyIntegrationTest.setProperties(registry);
    }

    @Autowired
    private IpToLocation ipToLocation;
    @Autowired
    private GeoLocation geoLocation;
    @Autowired
    private WorldCityService worldCityService;

    @Test
    public void testGetRegion()  {
        Assertions.assertEquals(ipToLocation.getNearest("86.23.52.41"), ipToLocation.getNearest("86.23.52.41"));
    }

    @Test
    public void testGetNearest()  {
        GeoLocationData nearest = geoLocation.getNearest(37.42301, -122.083352);
        System.out.println(nearest);
    }
}

