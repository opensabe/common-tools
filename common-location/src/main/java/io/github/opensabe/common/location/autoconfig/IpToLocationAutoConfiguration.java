package io.github.opensabe.common.location.autoconfig;

import io.github.opensabe.common.location.config.IpToLocationConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

//@Configuration(proxyBeanMethods = false)
@AutoConfiguration
@Import({IpToLocationConfiguration.class})
public class IpToLocationAutoConfiguration {
}
