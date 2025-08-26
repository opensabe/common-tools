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
package io.github.opensabe.common.dynamodb.service;

import java.util.function.Consumer;

import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

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
