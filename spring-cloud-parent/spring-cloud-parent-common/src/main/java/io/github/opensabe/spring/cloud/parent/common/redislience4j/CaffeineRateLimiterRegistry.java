/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.cloud.parent.common.redislience4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptyMap;

import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter;

public class CaffeineRateLimiterRegistry extends CaffeineResilienceRegistry<RateLimiter, RateLimiterConfig> implements RateLimiterRegistry {


    public CaffeineRateLimiterRegistry(Map<String, RateLimiterConfig> configs, RegistryEventConsumer<RateLimiter> registryEventConsumers, Map<String, String> tags) {
        super(configs, () -> RateLimiterConfig.ofDefaults(), registryEventConsumers, tags);
    }

    @Override
    public Set<RateLimiter> getAllRateLimiters() {
        return new HashSet<>(entryMap.values());
    }


    @Override
    public RateLimiter rateLimiter(final String name) {
        return rateLimiter(name, getDefaultConfig());
    }


    @Override
    public RateLimiter rateLimiter(String name, Map<String, String> tags) {
        return rateLimiter(name, getDefaultConfig(), tags);
    }


    @Override
    public RateLimiter rateLimiter(final String name, final RateLimiterConfig config) {
        return rateLimiter(name, config, emptyMap());
    }


    @Override
    public RateLimiter rateLimiter(String name, RateLimiterConfig config, Map<String, String> tags) {
        return computeIfAbsent(name, () -> new AtomicRateLimiter(name,
                Objects.requireNonNull(config, CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }


    @Override
    public RateLimiter rateLimiter(final String name,
                                   final Supplier<RateLimiterConfig> rateLimiterConfigSupplier) {
        return rateLimiter(name, rateLimiterConfigSupplier, emptyMap());
    }


    @Override
    public RateLimiter rateLimiter(String name,
                                   Supplier<RateLimiterConfig> rateLimiterConfigSupplier, Map<String, String> tags) {
        return computeIfAbsent(name, () -> new AtomicRateLimiter(name, Objects.requireNonNull(
                Objects.requireNonNull(rateLimiterConfigSupplier, SUPPLIER_MUST_NOT_BE_NULL).get(),
                CONFIG_MUST_NOT_BE_NULL), getAllTags(tags)));
    }


    @Override
    public RateLimiter rateLimiter(String name, String configName) {
        return rateLimiter(name, configName, emptyMap());
    }


    @Override
    public RateLimiter rateLimiter(String name, String configName, Map<String, String> tags) {
        return computeIfAbsent(name, () -> RateLimiter.of(name, getConfiguration(configName)
                .orElseThrow(() -> new ConfigurationNotFoundException(configName)), getAllTags(tags)));
    }
}
