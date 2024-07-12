package io.github.opensabe.common.alive.client.autoconfig;

import io.github.opensabe.common.alive.client.config.AliveConfiguration;
import io.github.opensabe.common.alive.client.config.AliveProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({AliveConfiguration.class})
@EnableConfigurationProperties(AliveProperties.class)
public class AliveAutoConfiguration {
}
