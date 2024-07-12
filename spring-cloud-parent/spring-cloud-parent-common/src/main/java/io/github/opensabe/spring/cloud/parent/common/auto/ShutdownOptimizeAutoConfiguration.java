package io.github.opensabe.spring.cloud.parent.common.auto;

import io.github.opensabe.spring.cloud.parent.common.config.ShutdownOptimizeConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ShutdownOptimizeConfiguration.class)
public class ShutdownOptimizeAutoConfiguration {
}
