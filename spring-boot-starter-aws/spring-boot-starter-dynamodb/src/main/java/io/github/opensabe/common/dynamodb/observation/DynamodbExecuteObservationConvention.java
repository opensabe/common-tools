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
           keyValues =  keyValues.and(TAG_HASH_KEY, context.getHashKey());
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
