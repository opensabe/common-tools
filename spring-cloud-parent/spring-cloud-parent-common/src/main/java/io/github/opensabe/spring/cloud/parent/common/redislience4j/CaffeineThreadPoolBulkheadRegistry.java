package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadConfig;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.registry.RegistryEventConsumer;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

public class CaffeineThreadPoolBulkheadRegistry extends CaffeineResilienceRegistry<ThreadPoolBulkhead, ThreadPoolBulkheadConfig> implements ThreadPoolBulkheadRegistry {


    public CaffeineThreadPoolBulkheadRegistry(Map<String, ThreadPoolBulkheadConfig> configs, RegistryEventConsumer<ThreadPoolBulkhead> registryEventConsumers, Map<String, String> tags) {
        super(configs, () -> ThreadPoolBulkheadConfig.ofDefaults(), registryEventConsumers, tags);
    }

    @Override
    public Set<ThreadPoolBulkhead> getAllBulkheads() {
        return new HashSet<>(entryMap.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name) {
        return bulkhead(name, emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, Map<String, String> tags) {
        return bulkhead(name, getDefaultConfig(), tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config) {
        return bulkhead(name, config, emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, ThreadPoolBulkheadConfig config, Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead
                .of(name, Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name,
                                       Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier) {
        return bulkhead(name, bulkheadConfigSupplier, emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name,
                                       Supplier<ThreadPoolBulkheadConfig> bulkheadConfigSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead.of(name, Objects.requireNonNull(
                Objects.requireNonNull(bulkheadConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName) {
        return bulkhead(name, configName, emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ThreadPoolBulkhead bulkhead(String name, String configName, Map<String, String> tags) {
        return computeIfAbsent(name, () -> ThreadPoolBulkhead.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }

    @Override
    public void close() throws Exception {
        for (ThreadPoolBulkhead bulkhead : getAllBulkheads()) {
            bulkhead.close();
        }
    }
}
