package io.github.opensabe.spring.cloud.parent.web.common.jfr;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.servlet.jfr")
public class HttpServerJFRProperties {
    private boolean enabled = true;
}
