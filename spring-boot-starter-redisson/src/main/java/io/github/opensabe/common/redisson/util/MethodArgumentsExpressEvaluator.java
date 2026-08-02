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
package io.github.opensabe.common.redisson.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
/**
 * 基于 Spring SpEL 的方法参数表达式求值器，用于解析 Redisson 注解中的动态锁名/限流名等。
 *
 * <p>无 {@code #} 占位符的表达式在解析失败时原样返回。
 *
 * @author heng.ma
 */

public class MethodArgumentsExpressEvaluator extends CachedExpressionEvaluator {

    private final Map<ExpressionKey, Expression> cache = new ConcurrentHashMap<>();

    private final BeanFactory beanFactory;

    /**
     * @param beanFactory Spring Bean 工厂，用于 SpEL 中解析 Bean 引用；可为 null
     */
    public MethodArgumentsExpressEvaluator(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected Expression getExpression(Method method, Class<?> target, String expression) {
        return super.getExpression(cache, new AnnotatedElementKey(method, target), expression);
    }


    /**
     * 解析方法级 SpEL 表达式为字符串。
     *
     * @param method 目标方法
     * @param target 调用目标（可为代理）
     * @param arguments 方法参数
     * @param expression SpEL 表达式或字面量
     * @return 解析结果；无 {@code #} 且解析失败时返回原表达式
     */
    public String resolve(Method method, Object target, Object[] arguments, String expression) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        try {
            return getExpression(method, targetClass, expression).getValue(createContext(method, target, targetClass, arguments), String.class);
        } catch (SpelParseException | EvaluationException e) {
            if (!StringUtils.contains(expression, "#")) {
                return expression;
            } else {
                throw e;
            }
        }
    }

    private MethodBasedEvaluationContext createContext(Method method, Object target, Class<?> targetClass, Object[] arguments) {
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(new RootObject(method, arguments, target, targetClass), method, arguments, getParameterNameDiscoverer());
        if (Objects.nonNull(beanFactory)) {
            context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return context;
    }

    private record RootObject(Method method, Object[] args, Object target, Class<?> targetClass) {

    }
}
