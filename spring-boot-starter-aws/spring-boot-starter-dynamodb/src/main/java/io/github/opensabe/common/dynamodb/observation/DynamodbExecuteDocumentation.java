package io.github.opensabe.common.dynamodb.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum DynamodbExecuteDocumentation implements ObservationDocumentation {

    SQL_EXECUTE_INSERT {
        @Override
        public String getName() {
            return "aws.execute.dynamodb.insert";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DynamodbExecuteObservationConvention.class;
        }

    },

    SQL_EXECUTE_SELECT {
        @Override
        public String getName() {
            return "aws.execute.dynamodb.select";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return DynamodbExecuteObservationConvention.class;
        }

    }
}
