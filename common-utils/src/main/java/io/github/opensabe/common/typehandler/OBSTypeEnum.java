package io.github.opensabe.common.typehandler;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * json typeHandler中大JSON保存位置
 * @author hengma
 */
@Getter
@AllArgsConstructor
public enum OBSTypeEnum {

    S3("s3ob"),

    DYNAMODB("dyna");

    private final String idShortName;
}
