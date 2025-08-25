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
package io.github.opensabe.common.s3.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum S3OperationObservationDocumentation implements ObservationDocumentation {
    S3_OPERATION {
        @Override
        public String getName() {
            return "s3.operate";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return S3OperationConvention.class;
        }
    };

    public enum S3_File_OPERATE_TAG implements KeyName {
        FILE_NAME {
            @Override
            public String asString() {
                return "s3.file.name";
            }
        },
        FILE_SIZE {
            @Override
            public String asString() {
                return "s3.file.size";
            }
        },
        OPERATE_TYPE {
            @Override
            public String asString() {
                return "s3.file.operate_type";
            }
        },
        OPERATE_SUCCESSFULLY {
            @Override
            public String asString() {
                return "s3.file.operate_successfully";
            }
        };
    }

}
