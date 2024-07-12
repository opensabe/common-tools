package io.github.opensabe.spring.cloud.parent.common.auto;

import io.github.opensabe.spring.cloud.parent.common.config.Log4j2Configuration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({Log4j2Configuration.class})
@AutoConfigureAfter(PrometheusMetricsExportAutoConfiguration.class)
public class Log4j2AutoConfiguration {
}
