package io.github.opensabe.common.entity.base.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * Basic MQ message format
 */
@Getter
@Setter
public class BaseMsg<T> {
    private String traceId;                         // traceId for auditing
    private Long ts;                                // current system timestamp
    private String src;                             // src system SrcEnum.val
    private String action;                          // customized action
    private T data;                                 // content data
}