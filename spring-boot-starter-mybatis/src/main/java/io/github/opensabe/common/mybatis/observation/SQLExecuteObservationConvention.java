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
package io.github.opensabe.common.mybatis.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * 统计SQL执行时间：SQL(mapper)或带事务的service方法名
 *
 * @author maheng
 */
public class SQLExecuteObservationConvention implements ObservationConvention<SQLExecuteContext> {

    public static final SQLExecuteObservationConvention DEFAULT = new SQLExecuteObservationConvention();

    private final String tagMethod = "method";
    private final String tagSuccess = "success";
    private final String tagTransactionId = "transactionId";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof SQLExecuteContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(SQLExecuteContext context) {
        return KeyValues.of(tagMethod, context.getMethod());
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(SQLExecuteContext context) {
        return KeyValues.of(tagMethod, context.getMethod())
                .and(tagSuccess, context.isSuccess() + "")
                .and(tagTransactionId, context.getTransactionName());
    }
}
