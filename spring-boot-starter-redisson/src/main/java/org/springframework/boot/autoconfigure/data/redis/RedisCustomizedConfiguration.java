package org.springframework.boot.autoconfigure.data.redis;

import com.google.common.collect.Maps;
import io.github.opensabe.common.redisson.config.MultiRedisProperties;
import io.github.opensabe.common.redisson.lettuce.MultiRedisLettuceConnectionFactory;
import io.lettuce.core.resource.ClientResources;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.List;
import java.util.Map;

@Log4j2
@ConditionalOnProperty(prefix = "spring.data.redis", value = "enable-multi", matchIfMissing = false)
@Configuration(proxyBeanMethods = false)
public class RedisCustomizedConfiguration {

    /**
     * @param builderCustomizers
     * @param clientResources
     * @param multiRedisProperties
     * @return
     * @see org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration
     */
    @Bean
    public MultiRedisLettuceConnectionFactory multiRedisLettuceConnectionFactory(
        ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
        ClientResources clientResources,
        MultiRedisProperties multiRedisProperties,
        ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
        ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
        ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
        RedisConnectionDetails connectionDetails, ObjectProvider<SslBundles> sslBundles,
        ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers
        ) {
        log.info("RedisCustomizedConfiguration-multiRedisLettuceConnectionFactory initialization starts... {}", multiRedisProperties.toString());
        Map<String, List<LettuceConnectionFactory>> connectionFactoryMap = Maps.newHashMap();
        Map<String, RedisProperties> multi = multiRedisProperties.getMulti();
        multi.forEach((k, v) -> {
            log.info("RedisCustomizedConfiguration-multiRedisLettuceConnectionFactory is initializing... {},{}", k,v.getHost());
            LettuceConnectionConfiguration lettuceConnectionConfiguration = new LettuceConnectionConfiguration(v,
                    standaloneConfigurationProvider,
                    sentinelConfigurationProvider,
                    clusterConfigurationProvider, connectionDetails, sslBundles);
            LettuceConnectionFactory lettuceConnectionFactory = lettuceConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientOptionsBuilderCustomizers, clientResources);
            lettuceConnectionFactory.setPipeliningFlushPolicy(LettuceConnection.PipeliningFlushPolicy.flushOnClose());
            lettuceConnectionFactory.setShareNativeConnection(false);
            connectionFactoryMap.put(k, List.of(lettuceConnectionFactory));
        });
        return new MultiRedisLettuceConnectionFactory(connectionFactoryMap);
    }

}
