package io.github.opensabe.common.mybatis.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * 事务及sql执行监控指标
 * @author maheng
 */
public enum SQLExecuteDocumentation implements ObservationDocumentation {

    /**
     * 监控mapper执行SQL
     */
    SQL_EXECUTE_MAPPER {
        @Override
        public String getName() {
            return "sql.execute.mapper";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return SQLExecuteObservationConvention.class;
        }

    },

    /**
     * 监控service执行事务
     */
    SQL_EXECUTE_TRANSACTION {
        @Override
        public String getName() {
            return "sql.execute.transaction";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return SQLExecuteObservationConvention.class;
        }

    }
}
