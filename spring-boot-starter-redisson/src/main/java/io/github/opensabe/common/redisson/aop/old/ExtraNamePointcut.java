package io.github.opensabe.common.redisson.aop.old;

import io.github.opensabe.common.redisson.aop.AbstractRedissonCachePointcut;
import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author heng.ma
 */
public abstract class ExtraNamePointcut<T extends AbstractRedissonProperties> extends AbstractRedissonCachePointcut<T> {


    protected ExtraNamePointcut(MethodArgumentsExpressEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    protected T findProperties(Method method, Class<?> targetClass) {
        T redissonProp = computeRedissonProperties(method, targetClass);
        if (redissonProp != null) {
            return redissonProp;
        }
        List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(targetClass);
        Optional<T> optional = fromClasses(allSuperclasses, method);
        if (optional.isEmpty()) {
            allSuperclasses = ClassUtils.getAllInterfaces(targetClass);
            optional = fromClasses(allSuperclasses, method);
        }
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    private Optional<T> fromClasses(List<Class<?>> list, Method method) {
        return list.stream()
                .map(i -> {
                    try {
                        return computeRedissonProperties(i.getMethod(method.getName(), method.getParameterTypes()), i);
                    } catch (NoSuchMethodException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    protected abstract T computeRedissonProperties(Method method, Class<?> clazz);

    protected static  <A extends Annotation> Pair<A, Integer> findParameterAnnotation(Method method, Class<A> annotationClass) {
        Annotation[][] as = method.getParameterAnnotations();
        for (int i = 0; i < as.length; i++) {
            Annotation[] ar = as[i];
            if (ArrayUtils.isEmpty(ar)) {
                continue;
            }
            //获取第一个 RedissonLockName 注解的参数
            Optional<A> op = Arrays.stream(ar)
                    .filter(a -> annotationClass.isInstance(a))
                    .map(a -> (A) a)
                    .findFirst();
            if (op.isPresent()) {
                return Pair.of(op.get(), i);
            }
        }
        return null;
    }
}
