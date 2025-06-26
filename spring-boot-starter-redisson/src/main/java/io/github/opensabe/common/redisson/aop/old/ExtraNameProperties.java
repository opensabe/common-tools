package io.github.opensabe.common.redisson.aop.old;

import io.github.opensabe.common.redisson.aop.AbstractRedissonProperties;
import io.github.opensabe.common.redisson.util.MethodArgumentsExpressEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * @author heng.ma
 */
public abstract class ExtraNameProperties extends AbstractRedissonProperties {

    private final int parameterIndex;
    private final String expression;


    private static final SpelExpressionParser parser = new SpelExpressionParser();
    private static final ParserContext context = new TemplateParserContext();

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
