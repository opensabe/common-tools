package io.github.opensabe.common.entity.base.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMQMessage {
    /**
     * sleuth的long traceId
     */
//    private Long openTracingTraceIdLong;
    /**
     * sleuth的long traceId high
     */
//    private Long openTracingTraceIdLongHigh;
    /**
     * sleuth的long spanId
     */
//    private Long openTracingSpanIdLong;
    /**
     * 业务的traceId
     */
    private String traceId;

    private String spanId;
    /**
     * 消息发送时间戳
     */
    private Long ts;
    /**
     * 消息源
     */
    private String src;
    /**
     * 业务动作
     */
    private String action;
    /**
     * 消息数据体,为了防止泛型嵌套丢失的问题，这里只能用String
     */
    private String data;
}
