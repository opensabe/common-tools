package io.github.opensabe.common.location.test.common;


import io.github.opensabe.common.location.properties.GeoPlacesProperties;
import io.github.opensabe.common.testcontainers.integration.SingleS3IntegrationTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Log4j2
@ExtendWith({SpringExtension.class, SingleS3IntegrationTest.class})
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "aws.location.enabled=false",
        "aws.s3.folderName="+ "testFolder/country",
        "aws.s3.defaultBucket=" + "test-bucket"
}, classes = GeoPlacesBaseTest.App.class)
@AutoConfigureObservability
public abstract class GeoPlacesBaseTest {

    @Autowired
    private GeoPlacesProperties geoPlacesProperties;

    @SpringBootApplication
    public static class App {
    }



}
