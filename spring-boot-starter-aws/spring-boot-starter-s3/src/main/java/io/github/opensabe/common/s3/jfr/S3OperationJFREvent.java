package io.github.opensabe.common.s3.jfr;

import io.github.opensabe.common.s3.observation.S3OperationContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Category({"observation", "s3"})
@Label("operation")
@StackTrace(false)
public class S3OperationJFREvent extends Event {
    @Label("file name")
    private String fileName;
    @Label("file size")
    private long fileSize;
    @Label("operate type")
    private String operateType;
    private boolean success;
    private String traceId;
    private String spanId;

    public S3OperationJFREvent(S3OperationContext s3OperationContext) {
        this.fileName = s3OperationContext.getFileName();
        this.operateType=s3OperationContext.getOperateType();
    }
}
