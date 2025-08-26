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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.opensabe.common.location.vo.IpLocation;
import lombok.Data;
import lombok.NoArgsConstructor;

public class IpGeoHttpFetchIpInfoService extends AbstractHttpFetchIpInfoService<IpGeoHttpFetchIpInfoService.Response> {
    @Override
    protected Class<Response> clazz() {
        return IpGeoHttpFetchIpInfoService.Response.class;
    }

    @Override
    protected String url(String ip) {
        return "https://api.ipgeolocation.io/ipgeo?include=hostname&ip=" + ip;
    }

    @Override
    protected HttpHeaders httpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("referer", "https://ipgeolocation.io/");
        httpHeaders.set("origin", "https://ipgeolocation.io");
        return httpHeaders;
    }

    @Override
    protected HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected IpLocation transfer(Response response) {
        return IpLocation.builder()
                .ip(response.ip)
                .city(response.city)
                .country(response.countryName)
                .latitude(response.latitude == null ? null : Double.valueOf(response.latitude))
                .longitude(response.longitude == null ? null : Double.valueOf(response.longitude))
                .build();
    }

    @NoArgsConstructor
    @Data
    public static class Response {
        @JsonProperty("ip")
        private String ip;
        @JsonProperty("hostname")
        private String hostname;
        @JsonProperty("continent_code")
        private String continentCode;
        @JsonProperty("continent_name")
        private String continentName;
        @JsonProperty("country_code2")
        private String countryCode2;
        @JsonProperty("country_code3")
        private String countryCode3;
        @JsonProperty("country_name")
        private String countryName;
        @JsonProperty("country_capital")
        private String countryCapital;
        @JsonProperty("state_prov")
        private String stateProv;
        @JsonProperty("district")
        private String district;
        @JsonProperty("city")
        private String city;
        @JsonProperty("zipcode")
        private String zipcode;
        @JsonProperty("latitude")
        private String latitude;
        @JsonProperty("longitude")
        private String longitude;
        @JsonProperty("is_eu")
        private Boolean isEu;
        @JsonProperty("calling_code")
        private String callingCode;
        @JsonProperty("country_tld")
        private String countryTld;
        @JsonProperty("languages")
        private String languages;
        @JsonProperty("country_flag")
        private String countryFlag;
        @JsonProperty("geoname_id")
        private String geonameId;
        @JsonProperty("isp")
        private String isp;
        @JsonProperty("connection_type")
        private String connectionType;
        @JsonProperty("organization")
        private String organization;
        @JsonProperty("asn")
        private String asn;
        @JsonProperty("currency")
        private CurrencyDTO currency;
        @JsonProperty("time_zone")
        private TimeZoneDTO timeZone;

        @NoArgsConstructor
        @Data
        public static class CurrencyDTO {
            @JsonProperty("code")
            private String code;
            @JsonProperty("name")
            private String name;
            @JsonProperty("symbol")
            private String symbol;
        }

        @NoArgsConstructor
        @Data
        public static class TimeZoneDTO {
            @JsonProperty("name")
            private String name;
            @JsonProperty("offset")
            private Integer offset;
            @JsonProperty("current_time")
            private String currentTime;
            @JsonProperty("current_time_unix")
            private Double currentTimeUnix;
            @JsonProperty("is_dst")
            private Boolean isDst;
            @JsonProperty("dst_savings")
            private Integer dstSavings;
        }
    }
}
