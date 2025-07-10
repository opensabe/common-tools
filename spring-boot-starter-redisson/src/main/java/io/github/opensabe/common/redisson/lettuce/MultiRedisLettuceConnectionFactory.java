package io.github.opensabe.common.redisson.lettuce;

import io.github.opensabe.common.redisson.config.MultiRedisProperties;
import io.github.opensabe.common.redisson.exceptions.RedissonClientException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class MultiRedisLettuceConnectionFactory
        implements InitializingBean, DisposableBean, RedisConnectionFactory, ReactiveRedisConnectionFactory {
    private final Map<String, List<LettuceConnectionFactory>> connectionFactoryMap;
    private final Map<String, AtomicInteger> positionMap;
    private static final ThreadLocal<String> currentRedis = new ThreadLocal<>();

    public MultiRedisLettuceConnectionFactory(Map<String, List<LettuceConnectionFactory>> connectionFactoryMap) {
        this.connectionFactoryMap = connectionFactoryMap;
        this.positionMap = new ConcurrentHashMap<>();
    }

    public void setCurrentRedis(String currentRedis) {
        if (!connectionFactoryMap.containsKey(currentRedis)) {
            throw new RedissonClientException("invalid currentRedis: " + currentRedis + ", it does not exists in configuration");
        }
        MultiRedisLettuceConnectionFactory.currentRedis.set(currentRedis);
    }

    @Override
    public void destroy() {
        connectionFactoryMap.values().stream().flatMap(Collection::stream).forEach(LettuceConnectionFactory::destroy);
    }

    @Override
    public void afterPropertiesSet() {
        connectionFactoryMap.values().stream().flatMap(Collection::stream).forEach(LettuceConnectionFactory::afterPropertiesSet);
    }

    private LettuceConnectionFactory currentLettuceConnectionFactory() {
        String currentRedis = MultiRedisLettuceConnectionFactory.currentRedis.get();
        if (StringUtils.isNotBlank(currentRedis)) {
            MultiRedisLettuceConnectionFactory.currentRedis.remove();
        } else {
            currentRedis = MultiRedisProperties.DEFAULT;
        }
        List<LettuceConnectionFactory> lettuceConnectionFactories = connectionFactoryMap.get(currentRedis);
        AtomicInteger position = positionMap
                .compute(currentRedis, (k, v) -> new AtomicInteger(ThreadLocalRandom.current().nextInt(0, 100)));
        int pos = position.getAndIncrement();
        return lettuceConnectionFactories.get(Math.abs(pos % lettuceConnectionFactories.size()));
    }

    @Override
    public ReactiveRedisConnection getReactiveConnection() {
        return currentLettuceConnectionFactory().getReactiveConnection();
    }

    @Override
    public ReactiveRedisClusterConnection getReactiveClusterConnection() {
        return currentLettuceConnectionFactory().getReactiveClusterConnection();
    }

    @Override
    public RedisConnection getConnection() {
        return currentLettuceConnectionFactory().getConnection();
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return currentLettuceConnectionFactory().getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return currentLettuceConnectionFactory().getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return currentLettuceConnectionFactory().getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return currentLettuceConnectionFactory().translateExceptionIfPossible(ex);
    }
}
