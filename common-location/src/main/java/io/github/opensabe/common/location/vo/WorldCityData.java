package io.github.opensabe.common.location.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public record WorldCityData(
        @Schema(description = "城市Unicode码", example = "艑saka") String unicode,
        @Schema(description = "ascii码", example = "Osaka") String ascii,
        @Schema(description = "经度", example = "34.752") String lat,
        @Schema(description = "维度", example = "135.4582") String lng,
        @Schema(description = "所在国家名称", example = "Japan") String country,
        @Schema(description = "国家ISO2编码", example = "JP") String iso2,
        @Schema(description = "国家ISO3编码", example = "JPN") String iso3,
        @Schema(description = "上一级行政级别，例如，保定的admin name是河北", example = "艑saka") String adminName,
        @Schema(description = "Primary 首都，admin，第一行政级别（省），minor 低级行政级别例如城市或者县等") String capital,
        @Schema(description = "估算的城市人口") String population,
        String id
) {

    public WorldCityData (String[] csvLine) {
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
