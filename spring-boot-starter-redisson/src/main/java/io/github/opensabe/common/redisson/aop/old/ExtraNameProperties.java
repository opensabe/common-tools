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
 * @author heng.ma
 */
public abstract class ExtraNameProperties extends AbstractRedissonProperties {

    private static final SpelExpressionParser parser = new SpelExpressionParser();
    private static final ParserContext context = new TemplateParserContext();
    private final int parameterIndex;
    private final String expression;

    private ExtraNameProperties(MethodArgumentsExpressEvaluator evaluator, String prefix, String name, int parameterIndex, String expression) {
        super(evaluator, prefix, name);
        this.parameterIndex = parameterIndex;
        this.expression = expression;
    }

    protected ExtraNameProperties(String prefix, String name, Integer parameterIndex, String expression) {
        this(null, prefix, name, parameterIndex, expression);
    }

    protected ExtraNameProperties(MethodArgumentsExpressEvaluator evaluator, String prefix, String name) {
        this(evaluator, prefix, name, -1, null);
    }

    @Override
    public String resolve(Method method, Object target, Object[] args) {
        if (parameterIndex != -1) {
            StringBuilder lockName = new StringBuilder();
            if (StringUtils.isNotBlank(expression)) {
                lockName.append(prefix).append(parser.parseExpression(expression, context).getValue(args[parameterIndex]));
            } else {
                lockName.append(prefix).append(args[parameterIndex]);
            }
            return lockName.toString();
        } else {
            return super.resolve(method, target, args);
        }
    }
}
