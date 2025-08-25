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
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.lang.NonNull;

import com.google.common.collect.Maps;

import io.github.opensabe.common.redisson.lettuce.MultiRedisLettuceConnectionFactory;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.log4j.Log4j2;

/**
 * 由于RedissonClient创建比较早，因此这里必须调整一下order
 *
 * @author heng.ma
 */
@Log4j2
@ConditionalOnProperty(prefix = "spring.data.redis", value = "enable-multi")
@Configuration(proxyBeanMethods = false)
public class MultiRedisConnectionFactoryConfiguration implements BeanPostProcessor, Ordered {

    private static final String CONFIGURATION_CLASS_NAME = "org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration";

    private static final String PROPERTY_CLASS_NAME = "org.redisson.spring.starter.PropertiesRedisConnectionDetails";

    private final MultiRedisProperties multiRedisProperties;

    private final MethodHandle propertyConstructor;
    private final MethodHandle configurationConstructor;
    private final MethodHandle redisConnectionFactory;
    private final MethodHandle redisConnectionFactoryVirtual;


    /**
     * 由于LettuceConnectionConfiguration不是公共类，因此使用反射来创建RedisConnectionFactory
     * org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration
     */
    public MultiRedisConnectionFactoryConfiguration(MultiRedisProperties multiRedisProperties) throws Throwable {
        this.multiRedisProperties = multiRedisProperties;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> configurationType = Class.forName(CONFIGURATION_CLASS_NAME);
        MethodHandles.Lookup configurationLookup = MethodHandles.privateLookupIn(configurationType, lookup);
        this.configurationConstructor = configurationLookup.findConstructor(configurationType, MethodType.methodType(void.class,
                RedisProperties.class,
                ObjectProvider.class,
                ObjectProvider.class,
                ObjectProvider.class,
                RedisConnectionDetails.class,
                ObjectProvider.class));

        this.redisConnectionFactory = configurationLookup.findVirtual(configurationType, "redisConnectionFactory", MethodType.methodType(LettuceConnectionFactory.class, ObjectProvider.class, ObjectProvider.class, ClientResources.class));
        this.redisConnectionFactoryVirtual = configurationLookup.findVirtual(configurationType, "redisConnectionFactoryVirtualThreads", MethodType.methodType(LettuceConnectionFactory.class, ObjectProvider.class, ObjectProvider.class, ClientResources.class));

        Class<?> propertyType = Class.forName(PROPERTY_CLASS_NAME);
        this.propertyConstructor = MethodHandles.privateLookupIn(propertyType, lookup).findConstructor(propertyType, MethodType.methodType(void.class, RedisProperties.class));
    }

    /**
     * 创建redissonClient需要RedissonProperties，因此需要替换掉默认的RedissonProperties
     *
     * @see org.redisson.spring.starter.RedissonAutoConfiguration
     */
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof RedisProperties) {
            return multiRedisProperties.getMulti().get(MultiRedisProperties.DEFAULT);
        }
        return bean;
    }

    @Bean
    @ConditionalOnThreading(Threading.PLATFORM)
    public MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactory(ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
                                                                                 ClientResources clientResources,
                                                                                 ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
                                                                                 ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
                                                                                 ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
                                                                                 ObjectProvider<SslBundles> sslBundles,
                                                                                 ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers) {
        log.info("RedisCustomizedConfiguration-multiRedisLettuceConnectionFactory initialization starts... {}", multiRedisProperties.toString());
        Map<String, List<LettuceConnectionFactory>> connectionFactoryMap = Maps.newHashMap();
        Map<String, RedisProperties> multi = multiRedisProperties.getMulti();
        multi.forEach((k, v) -> {
            log.info("RedisCustomizedConfiguration-multiRedisLettuceConnectionFactory is initializing... {},{}", k, v.getHost());
            try {
                Object property = propertyConstructor.invoke(v);
                Object configuration = configurationConstructor.invoke(v, standaloneConfigurationProvider, sentinelConfigurationProvider, clusterConfigurationProvider, property, sslBundles);
                LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory.bindTo(configuration)
                        .invokeExact(builderCustomizers, clientOptionsBuilderCustomizers, clientResources);
                lettuceConnectionFactory.setPipeliningFlushPolicy(LettuceConnection.PipeliningFlushPolicy.flushOnClose());
                lettuceConnectionFactory.setShareNativeConnection(false);
                connectionFactoryMap.put(k, List.of(lettuceConnectionFactory));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

        });
        return new MultiRedisLettuceConnectionFactory(connectionFactoryMap);
    }

    @Bean
    @ConditionalOnThreading(Threading.VIRTUAL)
    public MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactoryVirtual(ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
                                                                                        ClientResources clientResources,
                                                                                        ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
                                                                                        ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
                                                                                        ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
                                                                                        ObjectProvider<SslBundles> sslBundles,
                                                                                        ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers) {
        log.info("RedisCustomizedConfiguration-multiRedisLettuceConnectionFactoryVirtual initialization starts... {}", multiRedisProperties.toString());
        Map<String, List<LettuceConnectionFactory>> connectionFactoryMap = Maps.newHashMap();
        Map<String, RedisProperties> multi = multiRedisProperties.getMulti();
        multi.forEach((k, v) -> {
            log.info("RedisCustomizedConfiguration-multiRedisLettuceConnectionFactoryVirtual is initializing... {},{}", k, v.getHost());
            try {
                Object property = propertyConstructor.invokeExact(v);
                Object configuration = configurationConstructor.invoke(v, standaloneConfigurationProvider, sentinelConfigurationProvider, clusterConfigurationProvider, property, sslBundles);
                LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactoryVirtual.bindTo(configuration)
                        .invokeExact(builderCustomizers, clientOptionsBuilderCustomizers, clientResources);
                lettuceConnectionFactory.setPipeliningFlushPolicy(LettuceConnection.PipeliningFlushPolicy.flushOnClose());
                lettuceConnectionFactory.setShareNativeConnection(false);
                connectionFactoryMap.put(k, List.of(lettuceConnectionFactory));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }

        });
        return new MultiRedisLettuceConnectionFactory(connectionFactoryMap);
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
