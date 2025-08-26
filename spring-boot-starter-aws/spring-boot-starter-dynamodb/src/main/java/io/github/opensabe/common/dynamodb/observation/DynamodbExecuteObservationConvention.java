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

import org.apache.commons.lang3.StringUtils;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class DynamodbExecuteObservationConvention implements ObservationConvention<DynamodbExecuteContext> {

    public static DynamodbExecuteObservationConvention defaultConvention = new DynamodbExecuteObservationConvention();

    private final String tagMethod = "method";
    private final String tagRangeKey = "rangeKey";
    private final String tagHashKey = "hashKey";
    private final String tagExpression = "expression";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof DynamodbExecuteContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(DynamodbExecuteContext context) {
        return KeyValues.of(tagMethod, context.getMethod());
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(DynamodbExecuteContext context) {
        KeyValues keyValues = KeyValues.of(tagMethod, context.getMethod());
        if (StringUtils.isNotBlank(context.getHashKey())) {
            keyValues = keyValues.and(tagHashKey, context.getHashKey());
        }
        if (StringUtils.isNotBlank(context.getRangeKey())) {
            keyValues = keyValues.and(tagRangeKey, context.getRangeKey());
        }
        if (StringUtils.isNotBlank(context.getExpression())) {
            keyValues = keyValues.and(tagExpression, context.getExpression());
        }
        return keyValues;
    }
}
