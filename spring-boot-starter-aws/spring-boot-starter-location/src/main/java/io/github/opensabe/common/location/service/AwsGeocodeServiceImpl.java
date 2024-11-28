package io.github.opensabe.common.location.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.geoplaces.GeoPlacesClient;
import software.amazon.awssdk.services.geoplaces.model.*;

import java.util.List;

/**
 * @author changhongwei
 * @date 2024/11/26 17:56
 * @description:
 */
@Slf4j
public class AwsGeocodeServiceImpl implements GeocodeService {


    private final GeoPlacesClient geoPlacesClient;

    public AwsGeocodeServiceImpl(GeoPlacesClient geoPlacesClient) {
        this.geoPlacesClient = geoPlacesClient;
    }




    @Override
    public List<Double> getCoordinates(String address) {
        log.info("Fetching geocode for address: {}", address);
        try {
            GeocodeRequest geocodeRequest = GeocodeRequest.builder()
                    .queryText(address)
                    .build();
            GeocodeResponse geocodeResponse = geoPlacesClient.geocode(geocodeRequest);
            log.debug("GeocodeResponse: {}", geocodeResponse);
            if (geocodeResponse.resultItems().isEmpty()) {
                log.warn("No results found for address: {}", address);
                return null;
            }
            List<Double> position = geocodeResponse.resultItems().get(0).position();
            if (position.isEmpty()) {
                log.warn("No position data found in response for address: {}", address);
                return null;
            }
            return position;
        } catch (Throwable e) {
            log.error("Error while fetching geocode for address: {}", address, e);
        }
        return null;
    }
}
