package io.github.opensabe.common.dynamodb.service;

import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.function.Consumer;

/**
 * @author heng.ma
 */
public class QueryConditionalWrapper implements QueryConditional {

    private final QueryConditional delegate;
    private final Consumer<Expression> consumer;

    public QueryConditionalWrapper(QueryConditional delegate, Consumer<Expression> consumer) {
        this.delegate = delegate;
        this.consumer = consumer;
    }


    @Override
    public Expression expression(TableSchema<?> tableSchema, String indexName) {
        Expression expression = delegate.expression(tableSchema, indexName);
        consumer.accept(expression);
        return expression;
    }
}
