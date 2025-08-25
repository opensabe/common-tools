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
