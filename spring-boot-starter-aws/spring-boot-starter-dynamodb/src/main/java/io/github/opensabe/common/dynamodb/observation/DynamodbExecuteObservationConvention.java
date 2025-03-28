package io.github.opensabe.common.dynamodb.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import org.apache.commons.lang3.StringUtils;

public class DynamodbExecuteObservationConvention implements ObservationConvention<DynamodbExecuteContext> {

    public static DynamodbExecuteObservationConvention DEFAULT = new DynamodbExecuteObservationConvention();

    private final String TAG_METHOD = "method";
    private final String TAG_RANGE_KEY = "rangeKey";
    private final String TAG_HASH_KEY = "hashKey";
    private final String TAG_EXPRESSION = "expression";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof DynamodbExecuteContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(DynamodbExecuteContext context) {
        return KeyValues.of(TAG_METHOD,context.getMethod());
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(DynamodbExecuteContext context) {
        KeyValues keyValues = KeyValues.of(TAG_METHOD, context.getMethod());
        if (StringUtils.isNotBlank(context.getHashKey())) {
           keyValues =  keyValues.and(TAG_HASH_KEY, context.getMethod());
        }
        if (StringUtils.isNotBlank(context.getRangeKey())) {
            keyValues = keyValues.and(TAG_RANGE_KEY, context.getRangeKey());
        }
        if (StringUtils.isNotBlank(context.getExpression())) {
            keyValues = keyValues.and(TAG_EXPRESSION, context.getExpression());
        }
        return keyValues;
    }
}
