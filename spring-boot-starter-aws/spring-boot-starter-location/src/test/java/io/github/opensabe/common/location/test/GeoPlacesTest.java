package io.github.opensabe.common.location.test;

import io.github.opensabe.common.location.service.GeocodeService;
import io.github.opensabe.common.location.test.common.GeoPlacesBaseTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CreateFaceLivenessSessionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateFaceLivenessSessionRequestSettings;
import software.amazon.awssdk.services.rekognition.model.CreateFaceLivenessSessionResponse;
import software.amazon.awssdk.services.rekognition.model.LivenessOutputConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author changhongwei
 * @date 2024/11/27 19:52
 * @description: 地址测试
 */
@Log4j2
public class GeoPlacesTest extends GeoPlacesBaseTest {

    private String address = "Samuel Asabia House 35 Marina,Lagos,Nigeria";

    //    @Autowired
    private GeocodeService geocodeService;

    //    @Test
    public void testGetCoordinates() {
        List<Double> coordinates = geocodeService.getCoordinates(address);
        assertNotNull(coordinates, "Coordinates should not be null");
        log.info("Coordinates: {}", coordinates);
    }


    public static void main(String[] args) {
        RekognitionClient rekognitionClient = RekognitionClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
        LivenessOutputConfig outputConfig = LivenessOutputConfig.builder()
                .s3Bucket("test-bucket")
                .s3KeyPrefix("fake")
                .build();
        CreateFaceLivenessSessionRequestSettings settings = CreateFaceLivenessSessionRequestSettings.builder()
                .outputConfig(outputConfig)
                .auditImagesLimit(4)
                .build();

        CreateFaceLivenessSessionRequest request = CreateFaceLivenessSessionRequest.builder()
                .settings(settings)
                .kmsKeyId("233e")
                .clientRequestToken("123")
                .build();
        CreateFaceLivenessSessionResponse faceLivenessSession = rekognitionClient.createFaceLivenessSession(request);
        String sessionId = faceLivenessSession.sessionId();
        System.out.println(sessionId);


    }
}