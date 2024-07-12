package io.github.opensabe.common.idgenerator.autoconfig;

import io.github.opensabe.common.idgenerator.config.IdGeneratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({IdGeneratorConfiguration.class})
public class IdGeneratorAutoConfiguration {
}
