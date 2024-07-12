package io.github.opensabe.spring.cloud.parent.webflux.common.auto;

import io.github.opensabe.spring.cloud.parent.webflux.common.config.TracedPublisherConfiguration;
import io.github.opensabe.spring.cloud.parent.webflux.common.config.WebClientConfiguration;
import io.github.opensabe.spring.cloud.parent.webflux.common.handler.ExceptionConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@Import({WebClientConfiguration.class, ExceptionConfiguration.class, TracedPublisherConfiguration.class})
@AutoConfiguration
public class WebClientAutoConfiguration {
}
