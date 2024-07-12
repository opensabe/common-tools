package io.github.opensabe.common.location.service;

import javax.annotation.Nullable;

import io.github.opensabe.common.location.vo.GeoLocationData;

public interface GeoLocation {
	/**
	 * 获取最近的位置，如果相距过远，则返回null，即没找到
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	@Nullable
    GeoLocationData getNearest(double latitude, double longitude);
}
