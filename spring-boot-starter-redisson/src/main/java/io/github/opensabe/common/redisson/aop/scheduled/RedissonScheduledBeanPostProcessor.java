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
package io.github.opensabe.common.redisson.aop.scheduled;

import com.google.common.collect.Maps;
import io.github.opensabe.common.redisson.annotation.RedissonScheduled;
import io.github.opensabe.common.redisson.config.RedissonScheduleProperties;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.Map;

@Log4j2
public class RedissonScheduledBeanPostProcessor implements BeanPostProcessor {

    private final RedissonScheduleProperties redissonProperties;

    private final Map<String, Object> beanMap = Maps.newConcurrentMap();

    public RedissonScheduledBeanPostProcessor(RedissonScheduleProperties redissonProperties) {
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
