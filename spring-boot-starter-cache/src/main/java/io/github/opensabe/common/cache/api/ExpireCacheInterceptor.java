package io.github.opensabe.common.cache.api;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;

/**
 * @author heng.ma
 */
public class ExpireCacheInterceptor extends CacheInterceptor {

    private final ExpireCacheResolver cacheResolver;

    public ExpireCacheInterceptor(ExpireCacheResolver cacheResolver) {
        this.cacheResolver = cacheResolver;
    }

    @Override
    protected Collection<? extends Cache> getCaches(CacheOperationInvocationContext<CacheOperation> context, CacheResolver cacheResolver) {
        return super.getCaches(context, this.cacheResolver);
    }
}
