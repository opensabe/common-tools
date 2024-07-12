package io.github.opensabe.common.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.elasticsearch")
@Data
public class ElasticSearchProperties {
    private String addresses;
    private Boolean secure = false;
}
