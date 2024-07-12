package io.github.opensabe.common.entity.base.vo;


import lombok.Getter;
import lombok.Setter;

/**
 * Traceable request format
 * Extends this to enhance traceability
 */
@Getter
@Setter
public class TraceableReq {
    private String src;                               // src system SrcEnum.val
    private String traceId;                           // traceId for auditing
    private long ts;                                  // current system timestamp
}