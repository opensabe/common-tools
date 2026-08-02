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
package io.github.opensabe.common.location.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * WorldCityData 记录类型。
 */
public record WorldCityData(
        @Schema(description = "City Unicode name", example = "Osaka") String unicode,
        @Schema(description = "ASCII name", example = "Osaka") String ascii,
        @Schema(description = "Latitude", example = "34.752") String lat,
        @Schema(description = "Longitude", example = "135.4582") String lng,
        @Schema(description = "Country name", example = "Japan") String country,
        @Schema(description = "Country ISO2 code", example = "JP") String iso2,
        @Schema(description = "Country ISO3 code", example = "JPN") String iso3,
        @Schema(description = "Parent admin region name", example = "Osaka") String adminName,
        @Schema(description = "Capital type: primary, admin, minor") String capital,
        @Schema(description = "Estimated city population") String population,
        String id
) {

    public WorldCityData(String[] csvLine) {
        this(
                csvLine[0],
                csvLine[1],
                csvLine[2],
                csvLine[3],
                csvLine[4],
                csvLine[5],
                csvLine[6],
                csvLine[7],
                csvLine[8],
                csvLine[9],
                csvLine[10]
        );
    }
}
