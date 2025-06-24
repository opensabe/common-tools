package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.ObservedRedissonClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * @author heng.ma
 */
@Configuration(proxyBeanMethods = false)
public class RedissonClientBeanPostProcessor implements BeanPostProcessor {
    private final UnifiedObservationFactory unifiedObservationFactory;

    @Autowired
    @SuppressWarnings("unused")
    public RedissonClientBeanPostProcessor(UnifiedObservationFactory unifiedObservationFactory) {
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof RedissonClient delegate) {
            return new ObservedRedissonClient(delegate, unifiedObservationFactory);
        }
        return bean;
    }
}
