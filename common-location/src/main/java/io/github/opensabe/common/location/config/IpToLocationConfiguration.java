package io.github.opensabe.common.location.config;

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
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
public class IpToLocationConfiguration {

    @Bean
    public WorldCityService regionService () {
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
