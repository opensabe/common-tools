package io.github.opensabe.common.cache.api;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author heng.ma
 */
public class ExpireCacheResolver implements CacheResolver {

    private final CacheManager cacheManager;
    private final BeanFactory beanFactory;
    private final Map<Method, Collection<? extends Cache>> map;
    public ExpireCacheResolver(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.map = new ConcurrentHashMap<>();
        this.cacheManager = beanFactory.getBeanProvider(CacheManager.class).getIfAvailable();
    }


    @Override
    @NonNull
    public Collection<? extends Cache> resolveCaches( CacheOperationInvocationContext<?> context) {
        return map.computeIfAbsent(context.getMethod(), m -> {
            CacheManager cacheManager = this.cacheManager;
            if (context.getOperation() instanceof CacheOperation cacheOperation) {
                cacheManager = beanFactory.getBean(cacheOperation.getCacheManager(), ExpireCacheManager.class);
            }
            Expire expire = AnnotationUtils.getAnnotation(m, Expire.class);
            if (expire != null) {
                if (cacheManager instanceof ExpireCacheManager expireCacheManager) {
                    Duration ttl = Duration.of(expire.value(), expire.timeUnit().toChronoUnit());
                    return getCaches(expireCacheManager, context.getOperation().getCacheNames(), ttl);
                }else {
                    throw new IllegalStateException("CacheManager must be an instance of ExpireCacheManager while using @Expire");
                }
            }
            return getCaches(cacheManager, context.getOperation().getCacheNames());
        });
    }

    private Collection<? extends Cache> getCaches(ExpireCacheManager cacheManager, Collection<String> cacheNames, Duration ttl) {
        return cacheNames.stream().map(cacheName -> cacheManager.getCache(cacheName, ttl)).toList();
    }
    private Collection<? extends Cache> getCaches(CacheManager cacheManager, Collection<String> cacheNames) {
        return cacheNames.stream().map(cacheManager::getCache).toList();
    }
}
