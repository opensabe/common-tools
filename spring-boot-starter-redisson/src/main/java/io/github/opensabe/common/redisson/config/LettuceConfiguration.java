package io.github.opensabe.common.redisson.config;

import io.lettuce.core.event.DefaultEventPublisherOptions;
import io.lettuce.core.metrics.DefaultCommandLatencyCollector;
import io.lettuce.core.metrics.DefaultCommandLatencyCollectorOptions;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class LettuceConfiguration {
    private static final String DEFAULT_APPLICATION_NAME = "application";

    @Bean
    public ClientResourcesBuilderCustomizer clientResourcesBuilderCustomizer(ObservationRegistry observationRegistry, Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", DEFAULT_APPLICATION_NAME);
        return builder -> builder
                .tracing(new MicrometerTracing(observationRegistry, applicationName))
                .commandLatencyRecorder(new DefaultCommandLatencyCollector(DefaultCommandLatencyCollectorOptions.builder().enable().resetLatenciesAfterEvent(true).build()))
                .commandLatencyPublisherOptions(DefaultEventPublisherOptions.builder().eventEmitInterval(Duration.ofSeconds(10)).build())
                ;
    }

}
