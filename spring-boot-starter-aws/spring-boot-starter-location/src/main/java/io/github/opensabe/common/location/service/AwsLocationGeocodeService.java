package io.github.opensabe.common.location.service;

import io.github.opensabe.common.location.observation.LocationContext;
import io.github.opensabe.common.location.observation.LocationConvention;
import io.github.opensabe.common.location.observation.LocationDocumentation;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.geoplaces.GeoPlacesClient;
import software.amazon.awssdk.services.geoplaces.model.*;

import java.util.List;

/**
 * @author changhongwei
 * @date 2024/11/26 17:56
 * @description:
 */

@Log4j2
@Builder
public class AwsLocationGeocodeService implements GeocodeService {


    private final GeoPlacesClient geoPlacesClient;

    private final UnifiedObservationFactory unifiedObservationFactory;

    public AwsLocationGeocodeService(GeoPlacesClient geoPlacesClient, UnifiedObservationFactory unifiedObservationFactory) {
        this.geoPlacesClient = geoPlacesClient;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }


    @Override
    public List<Double> getCoordinates(String address) {
        log.info("getCoordinates: address={}", address);
        // 初始化 LocationContext
        LocationContext locationContext = new LocationContext(
                "getCoordinates",
                address,
                null,
                0,
                null
        );
        // 创建并启动 Observation
        Observation observation = LocationDocumentation.LOCATION
                .observation(
                        null,
                        LocationConvention.DEFAULT,
                        () -> locationContext,
                        unifiedObservationFactory.getObservationRegistry()
                ).start();

        try {
            // 构造 Geocode 请求
            GeocodeRequest geocodeRequest = GeocodeRequest.builder()
                    .queryText(address)
                    .build();
            long startTime = System.currentTimeMillis();
            // 调用 GeoPlacesClient 获取响应
            GeocodeResponse geocodeResponse = geoPlacesClient.geocode(geocodeRequest);
            locationContext.setResponse(geocodeResponse);
            log.info("GeocodeResponse: {}", geocodeResponse);
            // 校验结果是否为空
            if (geocodeResponse.resultItems().isEmpty()) {
                log.info("No results found for address: {}", address);
                return null;
            }
            // 提取地理位置
            List<Double> position = geocodeResponse.resultItems().get(0).position();
            if (position.isEmpty()) {
                log.info("No position data found in response for address: {}", address);
                return null;
            }
            // 设置成功状态
            locationContext.setSetSuccessful(true);
            locationContext.setExecutionTime(System.currentTimeMillis() - startTime);
            return position;
        } catch (Throwable e) {
            locationContext.setSetSuccessful(false);
            locationContext.setThrowable(e);
            log.error("Error while fetching geocode for address: {}", address, e.getMessage());
        }finally {
            observation.stop();
        }
        return null;
    }


    public ReverseGeocodeResponse reverseGeocode(List<Double> position) {
        log.info("Fetching reverse geocode for position: {}", position);
        // 初始化 LocationContext
        LocationContext locationContext = new LocationContext(
                "reverseGeocode",
                position,
                null,
                0,
                null
        );

        // 创建并启动 Observation
        Observation observation = LocationDocumentation.LOCATION
                .observation(
                        null,
                        LocationConvention.DEFAULT,
                        () -> locationContext,
                        unifiedObservationFactory.getObservationRegistry()
                ).start();
        long startTime = System.currentTimeMillis();
        try {
            // 构造 ReverseGeocode 请求
            ReverseGeocodeRequest request = ReverseGeocodeRequest.builder()
                    .queryPosition(position)
                    .maxResults(10)
                    .queryRadius(3000L)
                    .language("en")
                    .build();

            // 调用 GeoPlacesClient 获取响应
            ReverseGeocodeResponse reverseGeocodeResponse = geoPlacesClient.reverseGeocode(request);
            locationContext.setResponse(reverseGeocodeResponse);

            log.info("ReverseGeocodeResponse: {}", reverseGeocodeResponse);

            // 设置成功状态
            locationContext.setSetSuccessful(true);
            locationContext.setExecutionTime(System.currentTimeMillis() - startTime);

            return reverseGeocodeResponse;

        } catch (Throwable e) {
            // 捕获异常并设置上下文状态
            locationContext.setSetSuccessful(false);
            locationContext.setThrowable(e);
            log.error("Error while fetching reverse geocode for position: {}", position, e);
            return null;

        } finally {
            // 始终结束 Observation
            locationContext.setExecutionTime(System.currentTimeMillis() - startTime);
            observation.stop();
        }
    }
}
