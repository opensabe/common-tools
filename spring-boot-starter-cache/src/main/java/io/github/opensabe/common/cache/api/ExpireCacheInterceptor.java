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

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author heng.ma
 */
public class ExpireCacheInterceptor extends CacheInterceptor {

    private final ExpireCacheResolver cacheResolver;
    private final Map<Method, Boolean> map = new ConcurrentHashMap<>();
    public ExpireCacheInterceptor(ExpireCacheResolver cacheResolver) {
        this.cacheResolver = cacheResolver;
    }

    /**
     * 如果方法上包含 {@link Expire}注解，就使用 <code>ExpireCacheResolver</code>解析缓存实例，
     * 如果不包含，就走默认程序
     */
    @Override
    protected Collection<? extends Cache> getCaches(CacheOperationInvocationContext<CacheOperation> context, CacheResolver cacheResolver) {
        if (map.computeIfAbsent(context.getMethod(), m -> AnnotationUtils.getAnnotation(m, Expire.class) == null)) {
            return super.getCaches(context, cacheResolver);
        }
        return super.getCaches(context, this.cacheResolver);
    }
}
