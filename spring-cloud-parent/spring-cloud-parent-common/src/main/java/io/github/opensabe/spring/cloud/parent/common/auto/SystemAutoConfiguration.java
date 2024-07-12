package io.github.opensabe.spring.cloud.parent.common.auto;

import io.github.opensabe.spring.cloud.parent.common.config.SystemConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(SystemConfiguration.class)
public class SystemAutoConfiguration {
}
