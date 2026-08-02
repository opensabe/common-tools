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
package io.github.opensabe.common.redisson.aop.old;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;

/**
 * 支持参数级动态名称的 Redisson 属性基类（旧版 API）。
 * <p>
 * 当 {@link #parameterIndex} ≥ 0 时，从指定参数值（及可选 SpEL）构建键名。
 */
public abstract class ExtraNameProperties extends AbstractRedissonProperties {

    /** 参数 SpEL 解析器。 */
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /** 模板 SpEL 上下文（{@code #{...}}）。 */
    private static final ParserContext CONTEXT = new TemplateParserContext();

    /** 带 {@code *Name} 注解的参数索引；-1 表示使用 {@link #name} SpEL。 */
    private final int parameterIndex;

    /** 作用于参数值的 SpEL 模板；可为空。 */
    private final String expression;

    private ExtraNameProperties(MethodArgumentsExpressEvaluator evaluator, String prefix, String name, int parameterIndex, String expression) {
        super(evaluator, prefix, name);
        this.parameterIndex = parameterIndex;
        this.expression = expression;
    }

    /**
     * @param prefix 键前缀
     * @param name 默认名称（参数模式下降级使用）
     * @param parameterIndex 参数索引
     * @param expression 参数 SpEL
     */
    protected ExtraNameProperties(String prefix, String name, Integer parameterIndex, String expression) {
        this(null, prefix, name, parameterIndex, expression);
    }

    /**
     * @param evaluator SpEL 求值器
     * @param prefix 键前缀
     * @param name 名称 SpEL
     */
    protected ExtraNameProperties(MethodArgumentsExpressEvaluator evaluator, String prefix, String name) {
        this(evaluator, prefix, name, -1, null);
    }

    /** {@inheritDoc} */
    @Override
    public String resolve(Method method, Object target, Object[] args) {
        if (parameterIndex != -1) {
            StringBuilder lockName = new StringBuilder();
            if (StringUtils.isNotBlank(expression)) {
                lockName.append(prefix).append(PARSER.parseExpression(expression, CONTEXT).getValue(args[parameterIndex]));
            } else {
                lockName.append(prefix).append(args[parameterIndex]);
            }
            return lockName.toString();
        } else {
            return super.resolve(method, target, args);
        }
    }
}
