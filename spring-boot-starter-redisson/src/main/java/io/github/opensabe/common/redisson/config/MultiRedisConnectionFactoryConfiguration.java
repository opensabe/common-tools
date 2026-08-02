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
package io.github.opensabe.common.redisson.config;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.lang.NonNull;

import com.google.common.collect.Maps;

import io.github.opensabe.common.redisson.lettuce.MultiRedisLettuceConnectionFactory;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.log4j.Log4j2;

/**
 * 多 Redis Lettuce 连接工厂配置。
 * <p>
 * 因 {@link org.redisson.api.RedissonClient} 创建较早，本类实现 {@link Ordered} 与 {@link BeanPostProcessor}
 * 以在合适时机替换默认 {@link DataRedisProperties} 并注册 {@link MultiRedisLettuceConnectionFactory}。
 * Boot 4.1 的 {@code LettuceConnectionConfiguration} 非 public，故通过 {@link MethodHandle} 反射构建工厂。
 *
 * @author heng.ma
 */
@Log4j2
@ConditionalOnProperty(prefix = "spring.data.redis", value = "enable-multi")
@Configuration(proxyBeanMethods = false)
public class MultiRedisConnectionFactoryConfiguration implements BeanPostProcessor, Ordered {

    /** Boot 内部 Lettuce 连接配置类全限定名。 */
    private static final String CONFIGURATION_CLASS_NAME = "org.springframework.boot.data.redis.autoconfigure.LettuceConnectionConfiguration";

    /** Boot 内部基于属性的 Redis 连接详情类全限定名。 */
    private static final String PROPERTY_CLASS_NAME = "org.springframework.boot.data.redis.autoconfigure.PropertiesDataRedisConnectionDetails";

    /** 多 Redis 配置属性。 */
    private final MultiRedisProperties multiRedisProperties;

    /** 反射构造 {@code PropertiesDataRedisConnectionDetails} 的句柄。 */
    private final MethodHandle propertyConstructor;

    /** 反射构造 {@code LettuceConnectionConfiguration} 的句柄。 */
    private final MethodHandle configurationConstructor;

    /** 反射调用平台线程版 {@code redisConnectionFactory} 的句柄。 */
    private final MethodHandle redisConnectionFactory;

    /** 反射调用虚拟线程版 {@code redisConnectionFactoryVirtualThreads} 的句柄。 */
    private final MethodHandle redisConnectionFactoryVirtual;

    /**
     * 解析 Boot 内部类并初始化反射句柄。
     *
     * @param multiRedisProperties 多 Redis 配置
     * @throws Throwable 类加载或句柄绑定失败
     */
    public MultiRedisConnectionFactoryConfiguration(MultiRedisProperties multiRedisProperties) throws Throwable {
        this.multiRedisProperties = multiRedisProperties;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> configurationType = Class.forName(CONFIGURATION_CLASS_NAME);
        MethodHandles.Lookup configurationLookup = MethodHandles.privateLookupIn(configurationType, lookup);
        // Boot 4.1: + ObjectProvider<RedisStaticMasterReplicaConfiguration>
        this.configurationConstructor = configurationLookup.findConstructor(configurationType, MethodType.methodType(
                void.class,
                DataRedisProperties.class,
                ObjectProvider.class,
                ObjectProvider.class,
                ObjectProvider.class,
                ObjectProvider.class,
                DataRedisConnectionDetails.class));

        this.redisConnectionFactory = configurationLookup.findVirtual(configurationType, "redisConnectionFactory",
                MethodType.methodType(LettuceConnectionFactory.class, ObjectProvider.class, ObjectProvider.class, ClientResources.class));
        this.redisConnectionFactoryVirtual = configurationLookup.findVirtual(configurationType, "redisConnectionFactoryVirtualThreads",
                MethodType.methodType(LettuceConnectionFactory.class, ObjectProvider.class, ObjectProvider.class, ClientResources.class));

        Class<?> propertyType = Class.forName(PROPERTY_CLASS_NAME);
        this.propertyConstructor = MethodHandles.privateLookupIn(propertyType, lookup)
                .findConstructor(propertyType, MethodType.methodType(void.class, DataRedisProperties.class, SslBundles.class));
    }

    /**
     * 将容器中的 {@link DataRedisProperties} Bean 替换为 multi 配置里的 {@link MultiRedisProperties#DEFAULT} 条目，
     * 供 Redisson 自动配置读取默认连接。
     *
     * @param bean 待处理的 Bean
     * @param beanName Bean 名称
     * @return 替换后的属性或原 Bean
     * @see org.redisson.spring.starter.RedissonAutoConfiguration
     */
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof DataRedisProperties) {
            return multiRedisProperties.getMulti().get(MultiRedisProperties.DEFAULT);
        }
        return bean;
    }

    /**
     * 平台线程模式下创建多 Redis Lettuce 连接工厂。
     *
     * @return 按逻辑名称索引的 {@link MultiRedisLettuceConnectionFactory}
     */
    @Bean
    @ConditionalOnThreading(Threading.PLATFORM)
    public MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactory(ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
                                                                                 ClientResources clientResources,
                                                                                 ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
                                                                                 ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
                                                                                 ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
                                                                                 ObjectProvider<RedisStaticMasterReplicaConfiguration> masterReplicaConfigurationProvider,
                                                                                 ObjectProvider<SslBundles> sslBundles,
                                                                                 ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers) {
        log.info("MultiRedis Lettuce connection factory initialization started: {}", multiRedisProperties);
        Map<String, List<LettuceConnectionFactory>> connectionFactoryMap = Maps.newHashMap();
        Map<String, DataRedisProperties> multi = multiRedisProperties.getMulti();
        multi.forEach((k, v) -> {
            log.info("MultiRedis Lettuce connection factory initializing entry: key={}, host={}", k, v.getHost());
            try {
                Object property = propertyConstructor.invoke(v, sslBundles.getIfAvailable());
                Object configuration = configurationConstructor.invoke(v, standaloneConfigurationProvider, sentinelConfigurationProvider,
                        clusterConfigurationProvider, masterReplicaConfigurationProvider, property);
                LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory.bindTo(configuration)
                        .invokeExact(builderCustomizers, clientOptionsBuilderCustomizers, clientResources);
                lettuceConnectionFactory.setPipeliningFlushPolicy(LettuceConnection.PipeliningFlushPolicy.flushOnClose());
                lettuceConnectionFactory.setShareNativeConnection(false);
                connectionFactoryMap.put(k, List.of(lettuceConnectionFactory));
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create Lettuce connection factory for Redis entry: " + k, e);
            }

        });
        return new MultiRedisLettuceConnectionFactory(connectionFactoryMap);
    }

    /**
     * 虚拟线程模式下创建多 Redis Lettuce 连接工厂。
     *
     * @return 按逻辑名称索引的 {@link MultiRedisLettuceConnectionFactory}
     */
    @Bean
    @ConditionalOnThreading(Threading.VIRTUAL)
    public MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactoryVirtual(ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
                                                                                        ClientResources clientResources,
                                                                                        ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
                                                                                        ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
                                                                                        ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
                                                                                        ObjectProvider<RedisStaticMasterReplicaConfiguration> masterReplicaConfigurationProvider,
                                                                                        ObjectProvider<SslBundles> sslBundles,
                                                                                        ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers) {
        log.info("MultiRedis Lettuce connection factory (virtual threads) initialization started: {}", multiRedisProperties);
        Map<String, List<LettuceConnectionFactory>> connectionFactoryMap = Maps.newHashMap();
        Map<String, DataRedisProperties> multi = multiRedisProperties.getMulti();
        multi.forEach((k, v) -> {
            log.info("MultiRedis Lettuce connection factory (virtual threads) initializing entry: key={}, host={}", k, v.getHost());
            try {
                Object property = propertyConstructor.invoke(v, sslBundles.getIfAvailable());
                Object configuration = configurationConstructor.invoke(v, standaloneConfigurationProvider, sentinelConfigurationProvider,
                        clusterConfigurationProvider, masterReplicaConfigurationProvider, property);
                LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactoryVirtual.bindTo(configuration)
                        .invokeExact(builderCustomizers, clientOptionsBuilderCustomizers, clientResources);
                lettuceConnectionFactory.setPipeliningFlushPolicy(LettuceConnection.PipeliningFlushPolicy.flushOnClose());
                lettuceConnectionFactory.setShareNativeConnection(false);
                connectionFactoryMap.put(k, List.of(lettuceConnectionFactory));
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create Lettuce connection factory for Redis entry: " + k, e);
            }

        });
        return new MultiRedisLettuceConnectionFactory(connectionFactoryMap);
    }

    /** {@inheritDoc} — 确保在 Redisson 相关 Bean 之前执行后处理。 */
    @Override
    public int getOrder() {
        return 0;
    }
}
