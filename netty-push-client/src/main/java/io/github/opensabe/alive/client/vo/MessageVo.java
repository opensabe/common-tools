/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.alive.client.vo;

import com.google.protobuf.ByteString;
import io.github.opensabe.alive.protobuf.Message;

/**
 * @author lone
 */
public class MessageVo extends PushVo {

    public final byte[] body;
    public final long expiry;

    public MessageVo(String topic, byte[] body) {
        this(topic, body, null, Message.PushType.GROUP);
    }

    public MessageVo(String topic, byte[] body, String deviceId, Message.PushType pushType) {
        this(topic, body, deviceId, pushType, 0, null);
    }


    public MessageVo(String topic, byte[] body, String deviceId, Message.PushType pushType, long expiry) {
        this(topic, body, deviceId, pushType, expiry, null);
    }

    public MessageVo(String topic, byte[] body, String deviceId, Message.PushType pushType, long expiry, String accountId) {
        super(topic, deviceId, pushType, accountId);
        if (body == null) {
            throw new NullPointerException();
        }
        this.body = body;
        this.expiry = expiry;
    }

    public MessageVo(String topic, byte[] body, String deviceId, Message.PushType pushType, long expiry, String accountId, int requsetId) {
        super(topic, deviceId, pushType, accountId, requsetId);
        this.body = body;
        this.expiry = expiry;
    }


    public Message.Publish buildPublush(int requestId, int productCode) {
        Message.Publish.Builder builder = Message.Publish.newBuilder()
            .setRequestId(requestId)
            .setProductCode(productCode)
            .setTopic(topic)
            .setPushType(pushType)
            .setBody(ByteString.copyFrom(body));
        if (deviceId != null && !deviceId.isEmpty()) {
            builder.setDeviceId(deviceId);
        }
        if (expiry != 0) {
            builder.setExpiry(expiry);
        }
        if (accountId != null && !accountId.isEmpty()) {
            builder.setAccountId(accountId);
        }
        return builder.build();
    }

    public static void main(String[] args) {
        new MessageVo("aa", new byte[]{}).buildPublush(1, 1);
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
