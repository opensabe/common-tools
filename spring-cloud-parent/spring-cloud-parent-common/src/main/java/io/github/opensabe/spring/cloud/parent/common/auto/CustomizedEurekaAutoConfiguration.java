package io.github.opensabe.spring.cloud.parent.common.auto;

import io.github.opensabe.spring.cloud.parent.common.config.CustomizedEurekaConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CustomizedEurekaConfiguration.class})
public class CustomizedEurekaAutoConfiguration {
}
