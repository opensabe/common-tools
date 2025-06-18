package io.github.opensabe.common.entity.base.vo;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BaseMessage<T> {

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
     * 消息数据体
     */
    private T data;

    public BaseMessage(T data) {
        this.data = data;
    }
}
