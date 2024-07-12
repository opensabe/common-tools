package io.github.opensabe.common.redisson.config;

import org.redisson.config.Config;

@FunctionalInterface
public interface RedissonAutoConfigurationCustomizer {

    void customize(final Config configuration);
}