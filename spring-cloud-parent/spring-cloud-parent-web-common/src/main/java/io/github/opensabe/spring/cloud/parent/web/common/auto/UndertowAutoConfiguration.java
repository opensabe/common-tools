package io.github.opensabe.spring.cloud.parent.web.common.auto;

import io.github.opensabe.spring.cloud.parent.web.common.config.UndertowXNIOConfiguration;
import io.github.opensabe.spring.cloud.parent.web.common.config.WebServerConfiguration;
import io.github.opensabe.spring.cloud.parent.web.common.handler.ExceptionConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({WebServerConfiguration.class, UndertowXNIOConfiguration.class, ExceptionConfiguration.class})
public class UndertowAutoConfiguration {
}
