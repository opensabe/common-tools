package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.lettuce.core.event.DefaultEventPublisherOptions;
import io.lettuce.core.metrics.DefaultCommandLatencyCollector;
import io.lettuce.core.metrics.DefaultCommandLatencyCollectorOptions;
import io.lettuce.core.resource.DefaultClientResources;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.observability.MicrometerTracingAdapter;
import org.springframework.data.util.Lazy;

import java.time.Duration;

//@Configuration(proxyBeanMethods = false)
public class LettuceConfiguration {
    private static final String DEFAULT_APPLICATION_NAME = "application";

    /**
     * 每 10s 采集一次命令统计
     *
     * @return
     */
    @Bean
    public DefaultClientResources getDefaultClientResources(
            UnifiedObservationFactory unifiedObservationFactory, Environment environment
    ) {
        String applicationName = environment.getProperty("spring.application.name", DEFAULT_APPLICATION_NAME);
        DefaultClientResources build = DefaultClientResources.builder()
                .tracing(new MicrometerTracingAdapter(new LazyObservationRegistry(unifiedObservationFactory), applicationName))
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


    public static class LazyObservationRegistry implements ObservationRegistry {

        private final Lazy<ObservationRegistry> observationRegistry;

        public LazyObservationRegistry(UnifiedObservationFactory unifiedObservationFactory) {
            this.observationRegistry = Lazy.of(() -> unifiedObservationFactory.getObservationRegistry());
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
