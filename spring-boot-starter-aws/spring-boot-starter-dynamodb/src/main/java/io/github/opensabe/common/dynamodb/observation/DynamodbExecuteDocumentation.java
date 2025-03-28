package io.github.opensabe.common.dynamodb.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum DynamodbExecuteDocumentation implements ObservationDocumentation {

    PUT_ITEM {
        @Override
        public String getName() {
            return "aws.execute.dynamodb.insert";
        }
    },

    SELECT {
        @Override
        public String getName() {
            return "aws.execute.dynamodb.select";
        }
    },

    DELETE_ITEM {
        @Override
        public String getName() {
            return "aws.execute.dynamodb.delete";
        }
    },

    UPDATE_ITEM {
        @Override
        public String getName() {
            return "aws.execute.dynamodb.update";
        }
    }

    ;

    @Override
    public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
        return DynamodbExecuteObservationConvention.class;
    }
}
