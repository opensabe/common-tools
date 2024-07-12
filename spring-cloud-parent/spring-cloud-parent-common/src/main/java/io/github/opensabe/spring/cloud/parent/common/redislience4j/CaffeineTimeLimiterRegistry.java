package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

public class CaffeineTimeLimiterRegistry extends CaffeineResilienceRegistry<TimeLimiter, TimeLimiterConfig> implements TimeLimiterRegistry {


    public CaffeineTimeLimiterRegistry(Map<String, TimeLimiterConfig> configs, RegistryEventConsumer<TimeLimiter> registryEventConsumers, Map<String, String> tags) {
        super(configs, () -> TimeLimiterConfig.ofDefaults(), registryEventConsumers, tags);
    }

    @Override
    public Set<TimeLimiter> getAllTimeLimiters() {
        return new HashSet<>(entryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(final String name) {
        return timeLimiter(name, getDefaultConfig(), emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name, Map<String, String> tags) {
        return timeLimiter(name, getDefaultConfig(), tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(final String name, final TimeLimiterConfig config) {
        return timeLimiter(name, config, emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name,
                                   TimeLimiterConfig timeLimiterConfig, Map<String, String> tags) {
        return computeIfAbsent(name, () -> TimeLimiter.of(name,
                Objects.requireNonNull(timeLimiterConfig, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(final String name,
                                   final Supplier<TimeLimiterConfig> timeLimiterConfigSupplier) {
        return timeLimiter(name, timeLimiterConfigSupplier, emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name,
                                   Supplier<TimeLimiterConfig> timeLimiterConfigSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> TimeLimiter.of(name, Objects.requireNonNull(
                Objects.requireNonNull(timeLimiterConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeLimiter timeLimiter(String name, String configName) {
        return timeLimiter(name, configName, emptyMap());
    }

    @Override
    public TimeLimiter timeLimiter(String name, String configName, Map<String, String> tags) {
        TimeLimiterConfig config = getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName));
        return timeLimiter(name, config, tags);
    }
}
