package io.github.opensabe.common.dynamodb.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class DynamodbExecuteObservationConvention implements ObservationConvention<DynamodbExecuteContext> {

    public static DynamodbExecuteObservationConvention DEFAULT = new DynamodbExecuteObservationConvention();

    private final String TAG_METHOD = "method";
    private final String TAG_RANGE_KEY = "rangeKey";
    private final String TAG_HASH_KEY = "hashKey";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof DynamodbExecuteContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(DynamodbExecuteContext context) {
        return KeyValues.of(TAG_METHOD,context.getMethod(),TAG_RANGE_KEY,context.getRangeKey(),TAG_HASH_KEY,context.getHashKey());
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(DynamodbExecuteContext context) {
        return KeyValues.of(TAG_METHOD,context.getMethod())
                .and(TAG_RANGE_KEY, context.getRangeKey())
                .and(TAG_HASH_KEY,context.getHashKey());
    }
}
