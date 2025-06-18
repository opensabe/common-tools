package io.github.opensabe.common.entity.base.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class BaseMQMessage extends BaseMessage<String> {

    public BaseMQMessage() {
    }

    public BaseMQMessage(String data) {
        super(data);
    }

    public BaseMQMessage(String traceId, String spanId, Long ts, String src, String action, String data) {
        super(traceId, spanId, ts, src, action, data);
    }
}