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
