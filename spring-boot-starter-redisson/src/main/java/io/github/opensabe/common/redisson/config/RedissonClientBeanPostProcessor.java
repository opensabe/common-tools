package io.github.opensabe.common.redisson.config;

import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.redisson.observation.ObservedRedissonClient;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

/**
 * 将默认的RedissonClient替换为ObservedRedissonClient，
 * 因为RedissonClient创建比较早，因此这里必须调整一order，否则不经过BeanPostProcessor
 * @author heng.ma
 */
public class RedissonClientBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, Ordered {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof RedissonClient delegate) {
            return new ObservedRedissonClient(delegate, applicationContext.getBean(UnifiedObservationFactory.class));
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
