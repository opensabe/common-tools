package io.github.opensabe.common.dynamodb.observation;

import io.micrometer.observation.Observation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@ToString
public class DynamodbExecuteContext extends Observation.Context {

    private String method;

    private String hashKey;

    private String rangeKey;

    @Setter(AccessLevel.NONE)
    private String expression;

    public DynamodbExecuteContext(String method, AttributeValue hashKey, Optional<AttributeValue> rangeKey) {
        this.method = method;
        this.hashKey = resolveAttributeValue(hashKey);
        this.rangeKey = rangeKey.map(this::resolveAttributeValue).orElse(null);
    }
    public DynamodbExecuteContext(String method, Key key) {
        this (method, key.partitionKeyValue(), key.sortKeyValue());
    }
    public DynamodbExecuteContext(String method) {
        this.method = method;
    }

    public void setExpression (Expression expression) {
        Map<String, String> values = new HashMap<>(expression.expressionValues().size());
        expression.expressionValues().forEach((k, v) -> values.put(k, resolveAttributeValue(v)));
        this.expression = expression.expression();
        for (Map.Entry<String, String> entry : expression.expressionNames().entrySet()) {
            this.expression = this.expression.replaceAll(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            this.expression = this.expression.replaceAll(entry.getKey(), entry.getValue());
        }
    }

    private String resolveAttributeValue (AttributeValue value) {
        return switch (value.type()) {
            case S -> value.s();
            case N -> value.n();
            case B -> value.b().toString();
            case SS -> value.ss().toString();
            case NS -> value.ns().toString();
            case BS -> value.bs().toString();
            case M -> value.m().toString();
            case L -> value.l().toString();
            case BOOL -> value.bool().toString();
            case NUL -> null;
            case UNKNOWN_TO_SDK_VERSION -> null;
        };
    }
}
