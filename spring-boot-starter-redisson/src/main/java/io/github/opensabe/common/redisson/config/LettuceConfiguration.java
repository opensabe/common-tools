package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.secret.GlobalSecretManager;
import io.lettuce.core.event.DefaultEventPublisherOptions;
import io.lettuce.core.metrics.DefaultCommandLatencyCollector;
import io.lettuce.core.metrics.DefaultCommandLatencyCollectorOptions;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.tracing.Tracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.observability.MicrometerTracingAdapter;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class LettuceConfiguration {
    /**
     * 每 10s 采集一次命令统计
     *
     * @return
     */
    @Bean
    public DefaultClientResources getDefaultClientResources(
            ObservationRegistry observationRegistry, @Value("${spring.application.name}") String applicationName
    ) {
        DefaultClientResources build = DefaultClientResources.builder()
                .tracing(new MicrometerTracingAdapter(observationRegistry, applicationName))
                .commandLatencyRecorder(
                        new DefaultCommandLatencyCollector(
                                // define collector
                                DefaultCommandLatencyCollectorOptions.builder().enable().resetLatenciesAfterEvent(true).build()
                        )
                )
                .commandLatencyPublisherOptions(
                        //每 10s 采集一次命令统计
                        DefaultEventPublisherOptions.builder().eventEmitInterval(Duration.ofSeconds(10)).build()
                ).build();
        return build;
    }

    @Bean
    public RedisTemplateSecretFilter redisTemplateSecretFilter(GlobalSecretManager globalSecretManager) {
        return new RedisTemplateSecretFilter(globalSecretManager);
    }
}
