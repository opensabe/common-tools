package io.github.opensabe.spring.cloud.parent.web.common.jfr;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.cloud.openfeign.jfr")
public class FeignJFRProperties {
    private boolean enabled = true;
}
