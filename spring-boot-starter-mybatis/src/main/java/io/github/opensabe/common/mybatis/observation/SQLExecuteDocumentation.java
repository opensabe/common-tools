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

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * 事务及sql执行监控指标
 *
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
