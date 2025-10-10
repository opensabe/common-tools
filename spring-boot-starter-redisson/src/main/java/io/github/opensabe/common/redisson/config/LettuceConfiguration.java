package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.lettuce.core.event.DefaultEventPublisherOptions;
import io.lettuce.core.metrics.DefaultCommandLatencyCollector;
import io.lettuce.core.metrics.DefaultCommandLatencyCollectorOptions;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Lazy;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
public class LettuceConfiguration {
    private static final String DEFAULT_APPLICATION_NAME = "application";

    @Bean
    public ClientResourcesBuilderCustomizer clientResourcesBuilderCustomizer(UnifiedObservationFactory unifiedObservationFactory, Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", DEFAULT_APPLICATION_NAME);
        return builder -> builder
                .tracing(new MicrometerTracing(new LazyObservationRegistry(unifiedObservationFactory), applicationName))
                .commandLatencyRecorder(new DefaultCommandLatencyCollector(DefaultCommandLatencyCollectorOptions.builder().enable().resetLatenciesAfterEvent(true).build()))
                .commandLatencyPublisherOptions(DefaultEventPublisherOptions.builder().eventEmitInterval(Duration.ofSeconds(10)).build())
                ;
    }


    public static class LazyObservationRegistry implements ObservationRegistry {

        private final Lazy<ObservationRegistry> observationRegistry;

        public LazyObservationRegistry(UnifiedObservationFactory unifiedObservationFactory) {
            this.observationRegistry = Lazy.of(unifiedObservationFactory::getObservationRegistry);
        }

        @Override
        public Observation getCurrentObservation() {
            return observationRegistry.get().getCurrentObservation();
        }

        @Override
        public Observation.Scope getCurrentObservationScope() {
            return observationRegistry.get().getCurrentObservationScope();
        }

        @Override
        public void setCurrentObservationScope(Observation.Scope current) {
            observationRegistry.get().setCurrentObservationScope(current);
        }

        @Override
        public ObservationConfig observationConfig() {
            return observationRegistry.get().observationConfig();
        }

        @Override
        public boolean isNoop() {
            return observationRegistry.get().isNoop();
        }
    }

}
