package io.github.opensabe.common.mybatis.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

/**
 * 上班connection指标
 * @author maheng
 */
public class ConnectionObservationConvention implements ObservationConvention<ConnectionContext> {

    public static final ConnectionObservationConvention DEFAULT = new ConnectionObservationConvention();

//    @Override
//    public KeyValues getLowCardinalityKeyValues(ConnectionContext context) {
//        if (context.isConnect()) {
//            return KeyValues.of("success", context.isSuccess()+"")
//                    .and("createTime", context.getConnectedTime()+"")
//                    .and("waitThread", context.getWaitThread()+"")
//                    .and("activeCount", context.getActiveCount()+"");
//        }else {
//            return KeyValues.of("success", context.isSuccess()+"")
//                    .and("createTime", context.getConnectedTime()+"")
//                    .and("activeCount", context.getActiveCount()+"");
//        }
//    }

    @Override
    public KeyValues getHighCardinalityKeyValues(ConnectionContext context) {
        if (context.isConnect()) {
            return KeyValues.of("success", context.isSuccess()+"")
                    .and("createTime", context.getConnectedTime()+"")
                    .and("waitThread", context.getWaitThread()+"")
                    .and("activeCount", context.getActiveCount()+"")

                    .and("maxActive", context.getMaxActive()+"")
                    .and("maxWaitTime", context.getMaxWaitTime()+"")
                    .and("maxWaitThread", context.getMaxWaitThread()+"");
        }else {
            return KeyValues.of("success", context.isSuccess()+"")
                    .and("createTime", context.getConnectedTime()+"")
                    .and("activeCount", context.getActiveCount()+"");
        }
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ConnectionContext;
    }
}
