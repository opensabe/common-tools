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
