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
package io.github.opensabe.common.alive.client.message;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import io.github.opensabe.common.alive.client.message.enumeration.PushType;

public class MessageVo extends PushVo {
    public final String body;
    public final String id;
    public final long expiry;

    public MessageVo(String topic, String body, String id) {
        this(topic, body, id, (String) null, PushType.GROUP);
    }

    public MessageVo(String topic, String body, String id, String deviceId, PushType pushType) {
        this(topic, body, id, deviceId, pushType, 0L, (String) null);
    }

    public MessageVo(String topic, String body, String id, String deviceId, PushType pushType, long expiry, String accountId) {
        super(topic, deviceId, pushType, accountId);
        if (body == null) {
            throw new NullPointerException();
        } else {
            this.body = body;
            this.expiry = expiry;
            this.id = id;
        }
    }

    public MessageVo(String topic, String body, String id, String deviceId, PushType pushType, long expiry, String accountId, int requsetId) {
        super(topic, deviceId, pushType, accountId, requsetId);
        this.body = body;
        this.expiry = expiry;
        this.id = id;
    }

    public static void main(String[] args) {
        (new MessageVo("aa", "", "")).buildPublish(1, 1);
    }

    public Publish buildPublish(int requestId, int productCode) {
        Publish builder = new Publish();
        builder.setRequestId(requestId);
        builder.setProductCode(productCode);
        builder.setTopic(this.topic);
        builder.setPushType(this.pushType);
        builder.setId(this.id);
        builder.setBody(Base64.getEncoder().encode(this.body.getBytes(StandardCharsets.UTF_8)));
        if (this.deviceId != null && !this.deviceId.isEmpty()) {
            builder.setDeviceId(this.deviceId);
        }
        builder.setExpiry(this.expiry);
        if (this.accountId != null && !this.accountId.isEmpty()) {
            builder.setAccountId(this.accountId);
        }

        return builder;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("topic: ").append(this.topic);
        sb.append(" ,deviceId: ").append(this.deviceId);
        sb.append(" ,pushType: ").append(this.pushType);
        sb.append(" ,account: ").append(this.accountId);
        sb.append(" ,requestId: ").append(this.requestId);
        return sb.toString();
    }
}