package io.github.opensabe.common.alive.client.message;

import io.github.opensabe.common.alive.client.message.enumeration.RetCode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response extends MqMessage{
    private Integer requestId;
    private RetCode retCode;
    private String rightHost;
    private long messageId;
    private Integer sendNum;
    private String extra;

    public Response() {
    }

    public Response(Integer requestId, RetCode retCode, String rightHost, long messageId, Integer sendNum, String extra) {
        this.requestId = requestId;
        this.retCode = retCode;
        this.rightHost = rightHost;
        this.messageId = messageId;
        this.sendNum = sendNum;
        this.extra = extra;
    }
}
