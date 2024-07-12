package io.github.opensabe.alive.client.vo;

import io.github.opensabe.alive.protobuf.Message;

/**
 * Created by jianing on 2016/7/15.
 */
public class QueryVo extends PushVo {

    public QueryVo(String topic, String deviceId, Message.PushType pushType, String accountId) {
        super(topic, deviceId, pushType, accountId);
    }

    public QueryVo(String topic, String deviceId, Message.PushType pushType, String accountId, int requestId) {
        super(topic, deviceId, pushType, accountId, requestId);
    }

    public Object build(int requestId, int productCode) {
        Message.Query.Builder builder = Message.Query.newBuilder()
            .setRequestId(requestId)
            .setProductCode(productCode)
            .setTopic(topic)
            .setPushType(pushType);
        if (deviceId != null && !deviceId.isEmpty()) {
            builder.setDeviceId(deviceId);
        }
        if (accountId != null && !accountId.isEmpty()) {
            builder.setAccountId(accountId);
        }
        return builder.build();

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("topic: ").append(topic);
        sb.append(" ,deviceId: ").append(deviceId);
        sb.append(" ,pushType: ").append(pushType);
        sb.append(" ,account: ").append(accountId);
        sb.append(" ,requestId: ").append(requestId);
        return sb.toString();
    }
}
