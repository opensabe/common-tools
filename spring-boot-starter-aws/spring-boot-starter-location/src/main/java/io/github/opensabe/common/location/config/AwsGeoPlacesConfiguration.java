package io.github.opensabe.common.location.config;

import io.github.opensabe.common.location.jfr.LocationObservationToJFRGenerator;
import io.github.opensabe.common.location.properties.GeoPlacesProperties;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.opensabe.common.location.service.AwsLocationGeocodeService;
import io.github.opensabe.common.location.service.GeocodeService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.geoplaces.GeoPlacesClient;

import java.util.Objects;


@Log4j2
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "aws.location.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GeoPlacesProperties.class)
public class AwsGeoPlacesConfiguration {

    private final GeoPlacesProperties geoPlacesProperties;


    public AwsGeoPlacesConfiguration(GeoPlacesProperties geoPlacesProperties) {
        this.geoPlacesProperties = geoPlacesProperties;
    }


    /**
     * 创建 GeoPlacesClient Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public GeoPlacesClient geoPlacesClient() {
        this.validateProperties(geoPlacesProperties);
        return GeoPlacesClient.builder()
                .region(Region.of(geoPlacesProperties.getRegion()))
                .credentialsProvider(() ->
                        AwsBasicCredentials.create(geoPlacesProperties.getAccessKey(), geoPlacesProperties.getSecretKey())
                )
                .build();
    }
    private void validateProperties(GeoPlacesProperties properties) {
        if (Objects.isNull(properties.getAccessKey()) || Objects.isNull(properties.getSecretKey())) {
            throw new IllegalArgumentException("AWS access key and secret key must be provided.");
        }
        if (Objects.isNull(properties.getRegion())) {
            throw new IllegalArgumentException("AWS region must be provided.");
        }
    }

    @Bean
    public GeocodeService geocodeService(GeoPlacesClient geoPlacesClient, UnifiedObservationFactory unifiedObservationFactory) {
        log.info("GeocodeService bean is being created");
        return new AwsLocationGeocodeService(geoPlacesClient, unifiedObservationFactory);
    }

    @Bean
    public LocationObservationToJFRGenerator locationObservationToJFRGenerator(){
        return new LocationObservationToJFRGenerator();
    }
}
