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
package io.github.opensabe.common.cache.api;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * 有两种情况会使用该类解析缓存实例：
 * <ul>
 *     <li>方法上包含{@link Expire} 注解</li>
 *     <li><code>Cacheable</code>没有指定cacheManager（<code>ExpireCachingConfigurer</code>会指定默认解析器）</li>
 * </ul>
 *
 * @author heng.ma
 * @see io.github.opensabe.common.cache.config.ExpireCachingConfigurer
 */
public class ExpireCacheResolver implements CacheResolver {

    private final CompositeCacheManager cacheManager;
    private final BeanFactory beanFactory;
    private final Map<Method, CacheFunction> map;

    public ExpireCacheResolver(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.map = new ConcurrentHashMap<>();
        this.cacheManager = beanFactory.getBeanProvider(CompositeCacheManager.class).getIfAvailable();
    }


    /**
     * 只做两个操作：
     * <ul>
     *     <li>选择<code>CacheManager</code>还是<code>ExpireCacheManager</code></li>
     *     <li>选择获取code的方法<code>CacheManager.getCache(name)</code>还是<code>ExpireCacheManager.getCache(name, ttl)</code></li>
     * </ul>
     * <p>
     * 1. 如果<code>Cacheable</code>指定cacheManager，就要指定的这个，如果没有就根据 {@link Expire#cacheType()}来选
     * <br>
     * 2. 如果包含{@link Expire}注解就调用 <code>ExpireCacheManager.getCache(name, ttl)</code>
     * <br>
     * </p>
     *
     * @param context the context of the particular invocation
     * @throws ClassCastException 当包含{@link Expire}注解，但<code>CacheManager</code>指定的cacheManager不是{@link ExpireCacheManager}实例时
     * @see Expire#cacheType()
     * @see ExpireCacheManager#getCache(String, Duration)
     */
    @Override
    @NonNull
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        return map.computeIfAbsent(context.getMethod(), m -> {
            CacheManager cacheManager = this.cacheManager;
            if (context.getOperation() instanceof CacheOperation cacheOperation) {
                if (StringUtils.hasText(cacheOperation.getCacheManager())) {
                    cacheManager = beanFactory.getBean(cacheOperation.getCacheManager(), CacheManager.class);
                }
            }
            Expire expire = AnnotationUtils.getAnnotation(context.getMethod(), Expire.class);
            if (expire != null) {
                if (expire.cacheType() == CacheType.CAFFEINE) {
                    cacheManager = this.cacheManager.caffeineCacheManager();
                } else if (expire.cacheType() == CacheType.REDIS) {
                    cacheManager = this.cacheManager.redisCacheManager();
                }
            }
            return new CacheFunction(cacheManager, expire == null ? null : Duration.of(expire.value(), expire.timeUnit().toChronoUnit()));
        }).execute(context.getOperation().getCacheNames());

    }


    private record CacheFunction(CacheManager cacheManager, Duration ttl) {

        private Collection<? extends Cache> execute(Collection<String> cacheNames) {
            if (ttl == null) {
                return getCaches(cacheManager, cacheNames);
            } else {
                if (cacheManager instanceof ExpireCacheManager expireCacheManager) {
                    return getCaches(expireCacheManager, cacheNames, ttl);
                } else {
                    throw new ClassCastException("cacheManager must be instanceof ExpireCacheManager while @Expire is present");
                }
            }
        }

        private Collection<? extends Cache> getCaches(ExpireCacheManager cacheManager, Collection<String> cacheNames, Duration ttl) {
            return cacheNames.stream().map(cacheName -> cacheManager.getCache(cacheName, ttl)).toList();
        }

        private Collection<? extends Cache> getCaches(CacheManager cacheManager, Collection<String> cacheNames) {
            return cacheNames.stream().map(cacheManager::getCache).toList();
        }

    }
}
