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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.opensabe.common.location.vo.IpLocation;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class IpApiHttpFetchIpInfoService extends AbstractHttpFetchIpInfoService<IpApiHttpFetchIpInfoService.Response> {
    @Override
    protected Class<Response> clazz() {
        return IpApiHttpFetchIpInfoService.Response.class;
    }

    @Override
    protected String url(String ip) {
        return "https://ipapi.co/" + ip + "/json";
    }

    @Override
    protected HttpHeaders httpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("referer", "https://ipapi.co");
        httpHeaders.set("origin", "https://ipapi.co/");
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
                .region(response.region)
                .latitude(response.latitude)
                .longitude(response.longitude)
                .build();
    }

    @NoArgsConstructor
    @Data
    public static class Response {

        @JsonProperty("ip")
        private String ip;
        @JsonProperty("network")
        private String network;
        @JsonProperty("version")
        private String version;
        @JsonProperty("city")
        private String city;
        @JsonProperty("region")
        private String region;
        @JsonProperty("region_code")
        private String regionCode;
        @JsonProperty("country")
        private String country;
        @JsonProperty("country_name")
        private String countryName;
        @JsonProperty("country_code")
        private String countryCode;
        @JsonProperty("country_code_iso3")
        private String countryCodeIso3;
        @JsonProperty("country_capital")
        private String countryCapital;
        @JsonProperty("country_tld")
        private String countryTld;
        @JsonProperty("continent_code")
        private String continentCode;
        @JsonProperty("in_eu")
        private Boolean inEu;
        @JsonProperty("postal")
        private String postal;
        @JsonProperty("latitude")
        private Double latitude;
        @JsonProperty("longitude")
        private Double longitude;
        @JsonProperty("timezone")
        private String timezone;
        @JsonProperty("utc_offset")
        private String utcOffset;
        @JsonProperty("country_calling_code")
        private String countryCallingCode;
        @JsonProperty("currency")
        private String currency;
        @JsonProperty("currency_name")
        private String currencyName;
        @JsonProperty("languages")
        private String languages;
        @JsonProperty("country_area")
        private Double countryArea;
        @JsonProperty("country_population")
        private Integer countryPopulation;
        @JsonProperty("asn")
        private String asn;
        @JsonProperty("org")
        private String org;
    }
}
