package io.github.opensabe.common.location.service;

import javax.annotation.Nullable;

import io.github.opensabe.common.location.vo.GeoLocationData;

public interface IpToLocation {
	/**
	 * 通过 ip 获取最近的位置，如果相距过远，则返回null，即没找到
	 * @param ip
	 * @return
	 */
	@Nullable
    GeoLocationData getNearest(String ip);

	String getCountry(String ip);
}
