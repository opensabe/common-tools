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
package io.github.opensabe.common.location.config;

import java.util.List;
import java.util.stream.Collectors;

import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.opensabe.common.location.service.AbstractHttpFetchIpInfoService;
import io.github.opensabe.common.location.service.GeoLocation;
import io.github.opensabe.common.location.service.GeoLocationImpl;
import io.github.opensabe.common.location.service.IpApiHttpFetchIpInfoService;
import io.github.opensabe.common.location.service.IpGeoHttpFetchIpInfoService;
import io.github.opensabe.common.location.service.IpToLocation;
import io.github.opensabe.common.location.service.IpToLocationImpl;
import io.github.opensabe.common.location.service.LocationService;
import io.github.opensabe.common.location.service.LocationServiceImpl;
import io.github.opensabe.common.location.service.WorldCityService;
import io.github.opensabe.common.location.vo.GeoLocationData;
import io.github.opensabe.common.location.vo.WorldCityData;

@Configuration(proxyBeanMethods = false)
public class IpToLocationConfiguration {

    @Bean
    public WorldCityService regionService() {
        return new WorldCityService();
    }

    @Bean
    public IpToLocation getIpToLocation(StringRedisTemplate redisTemplate, GeoLocation geoLocation, List<AbstractHttpFetchIpInfoService<?>> abstractHttpFetchIpInfoServices) {
        return new IpToLocationImpl(redisTemplate, geoLocation, abstractHttpFetchIpInfoServices);
    }

    @Bean
    public GeoLocation readLocations(WorldCityService regionService) {
        List<WorldCityData> worldCities = regionService.getWorldCities();
        List<GeoLocationData> collect = worldCities.stream().map(city ->
                        GeoLocationData.builder()
                                .globalCoordinates(new GlobalCoordinates(Double.parseDouble(city.lat()), Double.parseDouble(city.lng())))
                                .city(city.ascii())
                                .country(city.country())
                                .build())
                .collect(Collectors.toList());
        return new GeoLocationImpl(collect);
    }

    @Bean
    public LocationService getLocationService(IpToLocation ipToLocation, GeoLocation geoLocation) {
        return new LocationServiceImpl(geoLocation, ipToLocation);
    }

    @Bean
    public IpApiHttpFetchIpInfoService ipApiHttpFetchIpInfoService() {
        return new IpApiHttpFetchIpInfoService();
    }

    @Bean
    public IpGeoHttpFetchIpInfoService ipGeoHttpFetchIpInfoService() {
        return new IpGeoHttpFetchIpInfoService();
    }
}
