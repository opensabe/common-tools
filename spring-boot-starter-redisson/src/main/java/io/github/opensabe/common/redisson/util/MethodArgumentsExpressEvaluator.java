package io.github.opensabe.common.redisson.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParseException;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author heng.ma
 */
public class MethodArgumentsExpressEvaluator extends CachedExpressionEvaluator {

    private final Map<ExpressionKey, Expression> cache = new ConcurrentHashMap<>();

    private final BeanFactory beanFactory;

    public MethodArgumentsExpressEvaluator(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected Expression getExpression(Method method, Class<?> target, String expression) {
        return super.getExpression(cache, new AnnotatedElementKey(method, target), expression);
    }


    @Nullable
    public String resolve (Method method, Object target, Object[] arguments, String expression) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        try {
            return getExpression(method, targetClass, expression).getValue(createContext(method, target, targetClass, arguments), String.class);
        }catch (SpelParseException | EvaluationException e) {
            if (!StringUtils.contains(expression, "#")) {
                return expression;
            }
        }
        return null;
    }

    private MethodBasedEvaluationContext createContext (Method method, Object target, Class<?> targetClass, Object[] arguments) {
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(new RootObject(method, arguments, target, targetClass), method, arguments, getParameterNameDiscoverer());
        if (Objects.nonNull(beanFactory)) {
            context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return context;
    }

    private record RootObject (Method method, Object[] args, Object target, Class<?> targetClass) {

    }
}
