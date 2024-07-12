package io.github.opensabe.spring.cloud.parent.common.auto;

import io.github.opensabe.spring.cloud.parent.common.config.Resilience4jConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        Resilience4jConfiguration.RetryConfiguration.class,
        Resilience4jConfiguration.CircuitBreakerConfiguration.class,
        Resilience4jConfiguration.BulkheadConfiguration.class,
        Resilience4jConfiguration.ThreadPoolBulkheadConfiguration.class,
        Resilience4jConfiguration.RateLimiterConfiguration.class,
        Resilience4jConfiguration.TimeLimiterConfiguration.class
})
public class Resilience4jAutoConfiguration {
}
