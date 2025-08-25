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

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.github.opensabe.common.location.vo.WorldCityData;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 解析csv中的数据
 *
 * @author maheng
 */
@Getter
public class WorldCityService {

    private final List<WorldCityData> worldCities;

    private final Map<String, WorldCityData> worldCountryMap;

    /**
     * 所有国家iso2编码
     */
    private final List<String> iso2s;

    /**
     * 国家iso3编码
     */
    private final List<String> iso3s;

    @SneakyThrows
    public WorldCityService() {
        //数据源下载更新：https://simplemaps.com/data/world-cities
        CSVReader reader =
                new CSVReaderBuilder(new InputStreamReader(WorldCityService.class.getResourceAsStream("/worldcities.csv"))).
                        withSkipLines(1). // Skiping firstline as it is header
                        build();
        this.worldCities = reader.readAll().stream().map(WorldCityData::new).toList();
        this.worldCountryMap = this.worldCities.stream().collect(Collectors.toMap(WorldCityData::country, v -> v, (l, r) -> l));
        this.iso2s = worldCities.stream().map(WorldCityData::iso2).distinct().toList();
        this.iso3s = worldCities.stream().map(WorldCityData::iso3).distinct().toList();
    }

}
