package io.github.opensabe.common.s3.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S3OperationContext extends Observation.Context{
    private String fileName;
    private long fileSize;
    private String operateType;
    private boolean success;
    public S3OperationContext(String fileName,String operateType){
        this.fileName = fileName;
        this.operateType = operateType;
    }
}
