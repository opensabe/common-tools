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
import software.amazon.awssdk.services.geoplaces.model.ReverseGeocodeResponse;

/**
 * @author changhongwei
 * @date 2024/11/26 17:56
 * @description: 根据地址获取地理坐标
 */
public interface GeocodeService {

    /**
     * 根据地址获取地理坐标
     *
     * @param address 地址
     * @return 经纬度列表 (longitude, latitude)
     */
    List<Double> getCoordinates(String address);


    /**
     * 根据坐标获取地理位置
     *
     * @param position
     * @return
     */
    ReverseGeocodeResponse reverseGeocode(List<Double> position);

}
