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

/**
 * MessageBuilder 类。
 * <p>消息Builder。</p>
 */
public class MessageBuilder {

/** MQ 主题名。 */
    private String topic;
/** 消息体。 */
    private byte[] body;
/** 推送类型。 */
    private PushType pushType;
/** 设备 ID。 */
    private String deviceId;
/** 账号 ID。 */
    private String accountId;
/** 过期时间戳。 */
    private long expiry = 0;

    private MessageBuilder() {
    }

/**
 * newBuilder 方法。
 */
    public static MessageBuilder newBuilder() {
        return new MessageBuilder();
    }

/**
 * setTopic 方法。
 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

/**
 * setBody 方法。
 */
    public void setBody(byte[] body) {
        this.body = body;
    }

/**
 * setPushType 方法。
 */
    public void setPushType(PushType pushType) {
        this.pushType = pushType;
    }

/**
 * setDeviceId 方法。
 */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

/**
 * setAccountId 方法。
 */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

/**
 * setExpiry 方法。
 */
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

/**
 * build 方法。
 */
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
