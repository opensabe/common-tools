package io.github.opensabe.common.location.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gavaghan.geodesy.GlobalCoordinates;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationData {
	public static final GeoLocationData EMPTY = new GeoLocationData();
	/**
	 * 其实是 city_ascii，我们不需要乱码
	 */
	private String city;
	private String country;
	private GlobalCoordinates globalCoordinates;

	public boolean isEmpty() {
		return this == EMPTY;
	}
}
