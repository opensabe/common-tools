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

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class S3OperationConvention implements ObservationConvention<S3OperationContext> {

    public static S3OperationConvention DEFAULT = new S3OperationConvention();

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof S3OperationContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(S3OperationContext context) {
        return KeyValues.of(
                S3OperationObservationDocumentation.S3_File_OPERATE_TAG.OPERATE_SUCCESSFULLY.withValue(String.valueOf(context.isSuccess())),
                S3OperationObservationDocumentation.S3_File_OPERATE_TAG.OPERATE_TYPE.withValue(context.getOperateType())
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(S3OperationContext context) {
        return KeyValues.of(
                S3OperationObservationDocumentation.S3_File_OPERATE_TAG.OPERATE_SUCCESSFULLY.withValue(String.valueOf(context.isSuccess())),
                S3OperationObservationDocumentation.S3_File_OPERATE_TAG.FILE_NAME.withValue(context.getFileName()),
                S3OperationObservationDocumentation.S3_File_OPERATE_TAG.OPERATE_TYPE.withValue(context.getOperateType()),
                S3OperationObservationDocumentation.S3_File_OPERATE_TAG.FILE_SIZE.withValue(String.valueOf(context.getFileSize()))
        );
    }

}
