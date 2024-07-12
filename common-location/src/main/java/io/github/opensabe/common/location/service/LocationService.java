package io.github.opensabe.common.location.service;

import javax.annotation.Nullable;

import io.github.opensabe.common.location.vo.GeoLocationData;

public interface LocationService {
	/**
	 * 同步微服务获取地理位置信息
	 * @param latitude 纬度
	 * @param longitude 经度
	 * @return
	 */
	@Nullable
    GeoLocationData getGeoLocationData(Double latitude, Double longitude);
}
