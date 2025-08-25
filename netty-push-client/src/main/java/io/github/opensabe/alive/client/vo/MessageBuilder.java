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

import io.github.opensabe.alive.protobuf.Message.PushType;

public class MessageBuilder {

    private String topic;
    private byte[] body;
    private PushType pushType;
    private String deviceId;
    private String accountId;
    private long expiry = 0;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void setPushType(PushType pushType) {
        this.pushType = pushType;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    private MessageBuilder() {
    }

    public static MessageBuilder newBuilder() {
        return new MessageBuilder();
    }

    public MessageVo build() {
        if (pushType == PushType.GROUP) {
            return new MessageVo(topic, body);
        } else if (pushType == PushType.SPECIAL) {
            return new MessageVo(topic, body, deviceId, pushType, expiry);
        } else if (pushType == PushType.MULTI) {
            return new MessageVo(topic, body, deviceId, pushType, expiry, accountId);
        }
        throw new IllegalArgumentException("unknow push type " + pushType);
    }

}
