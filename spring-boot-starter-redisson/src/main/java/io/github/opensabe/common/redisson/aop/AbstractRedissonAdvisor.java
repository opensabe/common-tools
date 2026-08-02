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
package io.github.opensabe.common.redisson.aop;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;
import org.springframework.lang.NonNull;

/**
 * Redisson AOP Advisor 抽象基类，绑定 {@link AbstractRedissonCachePointcut} 作为切点。
 *
 * @param <RedissonProp> 注解属性类型
 */
public abstract class AbstractRedissonAdvisor<RedissonProp extends AbstractRedissonProperties> extends AbstractBeanFactoryPointcutAdvisor {

    /** 缓存注解解析结果的切点实现。 */
    private final AbstractRedissonCachePointcut<RedissonProp> abstractRedissonCachePointcut;

    /**
     * @param abstractRedissonCachePointcut 切点
     */
    protected AbstractRedissonAdvisor(AbstractRedissonCachePointcut<RedissonProp> abstractRedissonCachePointcut) {
        this.abstractRedissonCachePointcut = abstractRedissonCachePointcut;
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Pointcut getPointcut() {
        return abstractRedissonCachePointcut;
    }
}
