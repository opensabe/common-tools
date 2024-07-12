package io.github.opensabe.common.alive.client.message;

import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import lombok.Data;

@Data
public class Publish extends MqMessage{

    private Integer requestId;

    /**
     *产品标识:表示是哪个产品
     */
    private Integer productCode;
    /**
     * id, remove duplicate
     */
    private String id;
    /**
     * 推送主题
     */
    private String topic;
    /**
     * 消息内容
     */
    private byte[] body;

    private PushType pushType;

    private String deviceId;

    private String accountId;

    private Long expiry;


}
