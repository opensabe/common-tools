package io.github.opensabe.common.location.test.common;


import io.github.opensabe.common.location.properties.GeoPlacesProperties;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Log4j2
@ExtendWith({SpringExtension.class})
@SpringBootTest(properties = {
        "eureka.client.enabled=false"
}, classes = GeoPlacesBaseTest.App.class)
@AutoConfigureObservability
public abstract class GeoPlacesBaseTest {

    @Autowired
    private GeoPlacesProperties geoPlacesProperties;

    @SpringBootApplication
    public static class App {
    }



}
