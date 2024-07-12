package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import io.github.resilience4j.core.registry.AbstractRegistry;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 将RegistryStory换成caffeine
 * @see AbstractRegistry
 * @param <E>   组件eg: Retry
 * @param <C>   组件配置 eg: RetryConfig
 */
public abstract class CaffeineResilienceRegistry<E, C> extends AbstractRegistry<E, C> {

    public CaffeineResilienceRegistry(Map<String,C> configs, Supplier<C> defaultConfig, RegistryEventConsumer<E> registryEventConsumer, Map<String, String> tags) {
        this(configs, defaultConfig, List.of(registryEventConsumer), tags);
    }
    public CaffeineResilienceRegistry(Map<String,C> configs, Supplier<C> defaultConfig, List<RegistryEventConsumer<E>> registryEventConsumers, Map<String, String> tags) {
        super(configs.computeIfAbsent(DEFAULT_CONFIG, key -> defaultConfig.get()), registryEventConsumers, tags, new CaffeineRegistryStore<>());
        this.configurations.putAll(configs);
    }
}
