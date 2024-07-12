package io.github.opensabe.common.redisson.aop;

import com.google.common.collect.Maps;
import io.github.opensabe.common.redisson.annotation.RedissonScheduled;
import io.github.opensabe.common.redisson.config.RedissonProperties;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.Map;

@Log4j2
public class RedissonScheduledBeanPostProcessor implements BeanPostProcessor {

    private final RedissonProperties redissonProperties;

    private final Map<String, Object> beanMap = Maps.newConcurrentMap();

    public RedissonScheduledBeanPostProcessor(RedissonProperties redissonProperties) {
        this.redissonProperties = redissonProperties;
    }

    public Map<String, Object> getBeanMap() {
        return beanMap;
    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!redissonProperties.isEnableSchedule()) {
            return bean;
        }
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        for (Method method : targetClass.getMethods()) {
            RedissonScheduled annotation = method.getAnnotation(RedissonScheduled.class);
            if (annotation != null) {
                beanMap.put(beanName, bean);
            }
        }
        if (bean instanceof AbstractRedissonScheduledService) {
            beanMap.put(beanName, bean);
        }
        return bean;
    }
}
