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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.sun.istack.NotNull;

import io.github.opensabe.common.location.vo.GeoLocationData;
import io.github.opensabe.common.location.vo.IpLocation;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class IpToLocationImpl implements IpToLocation {

    private final StringRedisTemplate redisTemplate;
    private final GeoLocation geoLocation;

    private final List<AbstractHttpFetchIpInfoService<?>> abstractHttpFetchIpInfoServices;
//    private final String IP_PREFIX = "ip_location:";


    public IpToLocationImpl(StringRedisTemplate redisTemplate, GeoLocation geoLocation, List<AbstractHttpFetchIpInfoService<?>> abstractHttpFetchIpInfoServices) {
        this.redisTemplate = redisTemplate;
        this.geoLocation = geoLocation;
        this.abstractHttpFetchIpInfoServices = abstractHttpFetchIpInfoServices;
    }

    private String getKey(String ip) {
        return "ip_location:" + ip;
    }

    private IpLocation getLocation(String ip) {
        String str = redisTemplate.opsForValue().get(getKey(ip));
        if (StringUtils.isNotBlank(str)) {
            return JsonUtil.parseObject(str, IpLocation.class);
        }
        IpLocation location = getLocationFromUrl(ip);
        if (location != null) {
            putLocationToRedis(ip, location);
        }
        return location;
    }

    private void putLocationToRedis(String ip, @NotNull IpLocation location) {
        location.setIp(ip);
        redisTemplate.opsForValue().setIfAbsent(getKey(ip), JsonUtil.toJSONString(location), 7, TimeUnit.DAYS);
    }

    @Nullable
    private IpLocation getLocationFromUrl(String ip) {
        Optional<IpLocation> first = abstractHttpFetchIpInfoServices.stream()
                .sorted(
                        Comparator.comparing(
                                abstractHttpFetchIpInfoService -> ThreadLocalRandom.current().nextInt()
                        )
                ).map(abstractHttpFetchIpInfoService -> {
                    try {
                        return abstractHttpFetchIpInfoService.getIpLocation(ip);
                    } catch (Throwable t) {
                        log.warn("IpToLocationImpl-getLocationFromUrl error: {}, {}, {}",
                                abstractHttpFetchIpInfoService.clazz().getSimpleName(),
                                ip, t.getMessage(), t
                        );
                        return null;
                    }
                }).filter(Objects::nonNull).findFirst();
        return first.orElse(null);
    }

    @Nullable
    @Override
    public GeoLocationData getNearest(String ip) {
        IpLocation location = getLocation(ip);
        log.info("IpToLocationImpl-getNearest: {} -> {}", ip, location);
        if (location != null && location.getLatitude() != null && location.getLongitude() != null) {
            return geoLocation.getNearest(location.getLatitude(), location.getLongitude());
        }
        return null;
    }

    @Override
    public String getCountry(String ip) {
        IpLocation location = getLocation(ip);
        log.info("IpToLocationImpl-getNearest: {} -> {}", ip, location);
        if (Objects.nonNull(location) && StringUtils.isNotEmpty(location.getCountry()))
            return location.getCountry();
        return null;
    }
}
