package io.github.opensabe.common.location.autoconf;


import io.github.opensabe.common.location.config.AwsGeoPlacesConfiguration;
import io.github.opensabe.common.location.properties.GeoPlacesProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;


@Import({AwsGeoPlacesConfiguration.class})
//@Configuration(proxyBeanMethods = false)
@AutoConfiguration
@EnableConfigurationProperties(GeoPlacesProperties.class)
public class AwsGeoPlacesAutoConfiguration {

}
