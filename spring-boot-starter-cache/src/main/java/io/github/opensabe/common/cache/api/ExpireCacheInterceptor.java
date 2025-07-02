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

    @Override
    protected Collection<? extends Cache> getCaches(CacheOperationInvocationContext<CacheOperation> context, CacheResolver cacheResolver) {
        if (map.computeIfAbsent(context.getMethod(), m -> AnnotationUtils.getAnnotation(m, Expire.class) == null)) {
            return super.getCaches(context, cacheResolver);
        }
        return super.getCaches(context, this.cacheResolver);
    }
}
