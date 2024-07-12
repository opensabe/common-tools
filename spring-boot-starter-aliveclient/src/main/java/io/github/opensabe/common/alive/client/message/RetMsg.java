package io.github.opensabe.common.alive.client.message;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RetMsg extends MqMessage{
    private String body;
    private String topic;
    private PushType pushType;
    private Integer messageId;

    public RetMsg() {
    }

    public RetMsg(String body, String topic, PushType pushType, Integer messageId) {
        this.body = body;
        this.topic = topic;
        this.pushType = pushType;
        this.messageId = messageId;
    }

    public static void main(String[] args) {
        RetMsg retMsg = new RetMsg();
        retMsg.setBody("aaaa");
        retMsg.setMessageId(111);
        retMsg.setPushType(PushType.SPECIAL);
        String s = JSON.toJSONString(retMsg);
        System.out.println(s);
        RetMsg retMsg1 = JSON.parseObject(s, RetMsg.class);
        System.out.println(retMsg1);
    }
}
