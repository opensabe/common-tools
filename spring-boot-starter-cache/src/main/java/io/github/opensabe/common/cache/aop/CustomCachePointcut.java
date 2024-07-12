package io.github.opensabe.common.cache.aop;

import io.github.opensabe.common.cache.utils.CacheHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Aspect
@AllArgsConstructor
public class CustomCachePointcut {

    private final CompositeCacheManager cacheManager;
    private final ApplicationContext context;

    @Pointcut("@annotation(org.springframework.cache.annotation.CachePut) || @annotation(org.springframework.cache.annotation.CacheEvict) || @annotation(org.springframework.cache.annotation.Cacheable)")
    public void cachePointcut() {}

    @Around("cachePointcut()")
    public Object cacheAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Annotation[] annotations = method.getAnnotations();

        Arrays.stream(annotations)
                .filter(annotation -> new HashSet<>(Arrays.asList("Cacheable", "CachePut", "CacheEvict")).contains(annotation.annotationType().getSimpleName()))
                .forEach(annotation -> {
                    List<CacheManager> cacheManagerList = CacheHelper.readFiled("cacheManagers", cacheManager, List.class);
                    ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
                    EvaluationContext evaluationContext =  new MethodBasedEvaluationContext(annotation, method, joinPoint.getArgs(), paramNameDiscoverer);
                    String cacheAnnotationName = annotation.annotationType().getSimpleName();

                    CacheAdvisor<?> cacheAdvisor = context.getBean(cacheAnnotationName.toLowerCase(), CacheAdvisor.class);
                    cacheAdvisor.setCacheOpt(annotation);
                    String keyField  = cacheAdvisor.getCacheKey();
                    String cacheName = CacheHelper.CACHE_NAME_PREFIX + cacheAdvisor.getCacheName();

                    cacheManagerList.stream().filter(manager -> manager instanceof RedisCacheManager)
                            .filter( manager -> manager.getCacheNames().contains(cacheName))
                            .forEach( manager -> {
                                RedisCacheConfiguration redisConfig = ((RedisCacheManager) manager).getCacheConfigurations().get(cacheName);
                                String key = StringUtils.isNotBlank(keyField)? (new SpelExpressionParser()).parseExpression(keyField).getValue(evaluationContext, String.class)
                                        : (String) SimpleKeyGenerator.generateKey(method, Arrays.stream(method.getParameters()).map(param -> evaluationContext.lookupVariable(param.getName())).toArray());
                                long ttl = Math.max(1, redisConfig.getTtl().getSeconds());
                                String keyPrefix = redisConfig.getKeyPrefixFor(cacheName);
                                cacheAdvisor.exec(cacheName, keyPrefix + key, ttl);
                            });
                });
        return joinPoint.proceed();
    }
}
