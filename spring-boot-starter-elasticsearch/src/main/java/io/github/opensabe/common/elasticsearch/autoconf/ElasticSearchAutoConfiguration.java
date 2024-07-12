package io.github.opensabe.common.elasticsearch.autoconf;

import io.github.opensabe.common.elasticsearch.config.ElasticSearchConfiguration;
import io.github.opensabe.common.elasticsearch.config.ElasticSearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        ElasticSearchConfiguration.class
})
@EnableConfigurationProperties(ElasticSearchProperties.class)
public class ElasticSearchAutoConfiguration {
}
