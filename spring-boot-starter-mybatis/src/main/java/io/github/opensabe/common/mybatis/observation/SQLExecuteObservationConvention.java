package io.github.opensabe.common.mybatis.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * 统计SQL执行时间：SQL(mapper)或带事务的service方法名
 * @author maheng
 */
public class SQLExecuteObservationConvention implements ObservationConvention<SQLExecuteContext> {

    public static SQLExecuteObservationConvention DEFAULT = new SQLExecuteObservationConvention();

    private final String TAG_METHOD = "method";
    private final String TAG_SUCCESS = "success";
    private final String TAG_TRANSACTION_ID = "transactionId";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof SQLExecuteContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(SQLExecuteContext context) {
        return KeyValues.of(TAG_METHOD,context.getMethod());
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(SQLExecuteContext context) {
        return KeyValues.of(TAG_METHOD,context.getMethod())
                .and(TAG_SUCCESS, context.isSuccess()+"")
                .and(TAG_TRANSACTION_ID,context.getTransactionName());
    }
}
