package io.github.opensabe.common.mybatis.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * connection监控指标
 * @author maheng
 */
public enum ConnectionDocumentation implements ObservationDocumentation {

    /**
     * 连接被使用
     */
    CONNECT {
        @Override
        public String getName() {
            return "mysql.connection.connect";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return ConnectionObservationConvention.class;
        }
    },

    /**
     * 释放连接
     */
    RELEASE {
        @Override
        public String getName() {
            return "mysql.connection.release";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return ConnectionObservationConvention.class;
        }
    }
}
