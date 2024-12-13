package io.github.opensabe.common.location.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = GeoPlacesProperties.PREFIX)
public class GeoPlacesProperties {
	
	public static final String PREFIX = "aws.location";

	private Boolean enabled = true;

	/**
	 * access_key
	 */
	private String accessKey;

	/**
	 * secret_key
	 */
	private String secretKey;
	/**
	 * 处于的 region
	 */
	private String region;

}
