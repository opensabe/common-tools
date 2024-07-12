package io.github.opensabe.common.location.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.opensabe.common.location.vo.GeoLocationData;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class GeoLocationImpl implements GeoLocation {
	private final Cache<String, GeoLocationData> cache = Caffeine.newBuilder().maximumSize(102400).build();
	private final Map<Integer, List<GeoLocationData>> geoLocationLatitudeDict;
	private final Map<Integer, List<GeoLocationData>> geoLocationLongitudeDict;

	public GeoLocationImpl(List<GeoLocationData> geoLocationDict) {
		this.geoLocationLatitudeDict = geoLocationDict.stream()
				.collect(Collectors.groupingBy(g -> (int) g.getGlobalCoordinates().getLatitude()));
		this.geoLocationLongitudeDict = geoLocationDict.stream()
				.collect(Collectors.groupingBy(g -> (int) g.getGlobalCoordinates().getLongitude()));
	}

	@Nullable
	@Override
	public GeoLocationData getNearest(double latitude, double longitude) {
		GeoLocationData geoLocationData = cache.get(latitude + "-" + longitude, k -> {
			//从经纬度 + 1 - 1 这个正方形范围中寻找
			int lat = (int) latitude;
			int lng = (int) longitude;
			GlobalCoordinates globalCoordinates = new GlobalCoordinates(latitude, longitude);
			List<GeoLocationData> toCalculate = Lists.newArrayList();
			for (int i = lat - 1; i <= lat + 1; i++) {
				for (int j = lng - 1; j <= lng + 1; j++) {
					joinCalculate(i, j, toCalculate);
				}
			}
			if (CollectionUtils.isEmpty(toCalculate)) {
				return GeoLocationData.EMPTY;
			}
			Optional<GeoLocationData> min = toCalculate.stream()
					.min(Comparator.comparingDouble(g -> getDistanceMeter(g.getGlobalCoordinates(), globalCoordinates)));
			//这里不返回 null 的原因是防止缓存对于不存在的重新计算
			return min.orElse(GeoLocationData.EMPTY);
		});
		GeoLocationData result = geoLocationData.isEmpty() ? null : geoLocationData;
		log.info("GeoLocationImpl-getNearest: {}, {} -> {}", latitude, longitude, result);
		return result;
	}

	private void joinCalculate(int lat, int lng, List<GeoLocationData> toCalculate) {
		List<GeoLocationData> geoLocationDataFromLat = geoLocationLatitudeDict.get(lat);
		List<GeoLocationData> geoLocationDataFromLng = geoLocationLongitudeDict.get(lng);
		if (CollectionUtils.isNotEmpty(geoLocationDataFromLat) && CollectionUtils.isNotEmpty(geoLocationDataFromLng)) {
			toCalculate.addAll(CollectionUtils.intersection(geoLocationDataFromLat, geoLocationDataFromLng));
		}
	}

	private double getDistanceMeter(GlobalCoordinates gpsFrom, GlobalCoordinates gpsTo) {
		//创建GeodeticCalculator，调用计算方法，传入坐标系、经纬度用于计算距离
		GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.Sphere, gpsFrom, gpsTo);
		return geoCurve.getEllipsoidalDistance();
	}
}
