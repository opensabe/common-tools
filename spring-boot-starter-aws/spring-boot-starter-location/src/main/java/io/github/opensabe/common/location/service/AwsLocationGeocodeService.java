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
package io.github.opensabe.common.location.service;

import java.util.List;

import io.github.opensabe.common.location.observation.LocationContext;
import io.github.opensabe.common.location.observation.LocationConvention;
import io.github.opensabe.common.location.observation.LocationDocumentation;
import io.github.opensabe.common.location.utils.Constants;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.geoplaces.GeoPlacesClient;
import software.amazon.awssdk.services.geoplaces.model.GeocodeRequest;
import software.amazon.awssdk.services.geoplaces.model.GeocodeResponse;
import software.amazon.awssdk.services.geoplaces.model.ReverseGeocodeRequest;
import software.amazon.awssdk.services.geoplaces.model.ReverseGeocodeResponse;

/**
 * @author changhongwei
 * @date 2024/11/26 17:56
 * @description:
 */

@Log4j2
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
        if (address == null || address.trim().isEmpty()) {
            log.warn("address is null");
            return List.of(); // 返回空列表，表示无结果
        }
        // 初始化 LocationContext
        LocationContext locationContext = new LocationContext(
                Constants.GETCOORDINATES_METHODNAME,
                address,
                null,
                0,
                false,
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
            // 构造 Geocode 请求
            GeocodeRequest geocodeRequest = GeocodeRequest.builder()
                    .queryText(address)
                    .build();
            // 调用 GeoPlacesClient 获取响应
            GeocodeResponse geocodeResponse = geoPlacesClient.geocode(geocodeRequest);
            locationContext.setResponse(geocodeResponse);
            log.info("GeocodeResponse: {}", geocodeResponse);
            // 校验结果是否为空
            if (geocodeResponse.resultItems().isEmpty()) {
                log.info("No results found for address: {}", address);
                return List.of(); // 返回空列表，表示无结果
            }
            // 提取地理位置
            List<Double> position = geocodeResponse.resultItems().get(0).position();
            if (position.isEmpty()) {
                log.info("No position data found in response for address: {}", address);
                return List.of(); // 返回空列表，表示无位置数据
            }

            // 设置成功状态
            locationContext.setSuccessful(true);
            return position;
        } catch (Throwable e) {
            observation.error(e);
            locationContext.setSuccessful(false);
            locationContext.setThrowable(e);
            log.error("Error while fetching geocode for address: {}", address, e);
            throw e;
        } finally {
            locationContext.setExecutionTime(System.currentTimeMillis() - startTime);
            observation.stop();
        }
    }


    @Override
    public ReverseGeocodeResponse reverseGeocode(List<Double> position) {
        log.info("Fetching reverse geocode for position: {}", position);
        if (position == null || position.isEmpty()) {
            log.warn("position is null");
            return ReverseGeocodeResponse.builder().build();
        }
        // 初始化 LocationContext
        LocationContext locationContext = new LocationContext(
                Constants.REVERSEGEOCODE_METHODNAME,
                position,
                null,
                0,
                false,
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
                    .maxResults(Constants.REVERSE_MAXRESULTS)
                    .queryRadius(Constants.REVERSE_QUERYRADIUS)
                    .language(Constants.REVERSE_LANGUAGE)
                    .build();

            // 调用 GeoPlacesClient 获取响应
            ReverseGeocodeResponse reverseGeocodeResponse = geoPlacesClient.reverseGeocode(request);
            locationContext.setResponse(reverseGeocodeResponse);

            log.info("ReverseGeocodeResponse: {}", reverseGeocodeResponse);

            // 设置成功状态
            locationContext.setSuccessful(true);
            return reverseGeocodeResponse;

        } catch (Throwable e) {
            // 捕获异常并设置上下文状态
            locationContext.setSuccessful(false);
            locationContext.setThrowable(e);
            log.error("Error while fetching reverse geocode for position: {}", position, e);
            throw e;
        } finally {
            // 始终结束 Observation
            locationContext.setExecutionTime(System.currentTimeMillis() - startTime);
            observation.stop();
        }
    }
}
