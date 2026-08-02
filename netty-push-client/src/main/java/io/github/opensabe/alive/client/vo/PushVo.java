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

import org.apache.commons.lang3.StringUtils;

import io.github.opensabe.alive.client.impl.ClientConnection;
import io.github.opensabe.alive.protobuf.Message;

/**
 * Alive 推送目标值对象（topic、设备/账号与推送类型）。
 */
public class PushVo {

    /** MQ 主题名。 */
    public final String topic;

    /** 设备 ID（{@code SPECIAL} 推送时必填）。 */
    public final String deviceId;

    /** 推送类型。 */
    public final Message.PushType pushType;

    /** 账号 ID（{@code MULTI} 推送时必填）。 */
    public final String accountId;

    /** 请求 ID。 */
    public int requestId = 0;

    public PushVo(String topic, String deviceId, Message.PushType pushType, String accountId) {
        if (StringUtils.isBlank(topic)) {
            throw new NullPointerException();
        }
        if (pushType == null) {
            pushType = Message.PushType.GROUP;
        }
        if (pushType == Message.PushType.SPECIAL && StringUtils.isBlank(deviceId)) {
            throw new NullPointerException("deviceId is required for SPECIAL push");
        }
        if (pushType == Message.PushType.MULTI && StringUtils.isBlank(accountId)) {
            throw new NullPointerException("accountId is required for MULTI push");
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

/**
 * 生成线程本地请求 ID。
 */
    public static int generateRequestId() {
        return ClientConnection.getReqeustId();
    }

/**
 * getRequestId 方法。
 */
    public int getRequestId() {
        return requestId;
    }

/**
 * setRequestId 方法。
 */
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }
}
