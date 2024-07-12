package io.github.opensabe.alive.client.vo;

import io.github.opensabe.alive.client.impl.ClientConnection;
import io.github.opensabe.alive.protobuf.Message;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by jianing on 2016/7/15.
 */
public class PushVo {
    /**
     * 主题名称
     */
    public final String topic;
    /**
     * 设备id(pushType为Special类型时，必传)
     */
    public final String deviceId;
    public final Message.PushType pushType;
    /**
     * 账户id(pushType为Multi时，必传)
     */
    public final String accountId;
    public int requestId = 0;

    public PushVo(String topic, String deviceId, Message.PushType pushType, String accountId) {
        if (StringUtils.isBlank(topic)) {
            throw new NullPointerException();
        }
        if (pushType == null) {
            pushType = Message.PushType.GROUP;
        }
        if (pushType == Message.PushType.SPECIAL && StringUtils.isBlank(deviceId)) {
            throw new NullPointerException("单推消息时,deviceId不能为空");
        }
        if (pushType == Message.PushType.MULTI && StringUtils.isBlank(accountId)) {
            throw new NullPointerException("组推消息时,accountId不能为空");
        }

        this.topic = topic;
        this.deviceId = deviceId;
        this.pushType = pushType;
        this.accountId = accountId;
        requestId = generateRequestId();
    }

    public PushVo(String topic, String deviceId, Message.PushType pushType, String accountId, int requestId) {
        this(topic, deviceId, pushType, accountId);
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public static int generateRequestId() {
        return ClientConnection.getReqeustId();
    }
}
