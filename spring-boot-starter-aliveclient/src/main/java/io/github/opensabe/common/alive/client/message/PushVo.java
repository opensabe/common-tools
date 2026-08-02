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

import java.util.concurrent.atomic.AtomicInteger;

import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import io.micrometer.core.instrument.util.StringUtils;

/**
 * PushVo 类。
 * <p>推送Vo。</p>
 */
public class PushVo {

/** REQUEST_ID_THREAD_LOCAL 字段。 */
    private static final ThreadLocal<AtomicInteger> REQUEST_ID_THREAD_LOCAL = ThreadLocal.withInitial(() -> new AtomicInteger());

/** MQ 主题名。 */
    public final String topic;
/** 设备 ID。 */
    public final String deviceId;
/** 推送类型。 */
    public final PushType pushType;
/** 账号 ID。 */
    public final String accountId;
/** 请求 ID。 */
    public int requestId;

    public PushVo(String topic, String deviceId, PushType pushType, String accountId) {
        this.requestId = 0;
        if (StringUtils.isBlank(topic)) {
            throw new NullPointerException();
        } else {
            if (pushType == null) {
                pushType = PushType.GROUP;
            }

            if (pushType == PushType.SPECIAL && StringUtils.isBlank(deviceId)) {
                throw new NullPointerException("deviceId is required for SPECIAL push");
            } else if (pushType == PushType.MULTI && StringUtils.isBlank(accountId)) {
                throw new NullPointerException("accountId is required for MULTI push");
            } else {
                this.topic = topic;
                this.deviceId = deviceId;
                this.pushType = pushType;
                this.accountId = accountId;
                this.requestId = generateRequestId();
            }
        }
    }

    public PushVo(String topic, String deviceId, PushType pushType, String accountId, int requestId) {
        this(topic, deviceId, pushType, accountId);
        this.requestId = requestId;
    }

/**
 * 生成线程本地请求 ID。
 */
    public static int generateRequestId() {
        if (REQUEST_ID_THREAD_LOCAL.get().intValue() < 0) {
            REQUEST_ID_THREAD_LOCAL.remove();
        }
        return REQUEST_ID_THREAD_LOCAL.get().incrementAndGet();
    }

/**
 * getRequestId 方法。
 */
    public int getRequestId() {
        return this.requestId;
    }

/**
 * setRequestId 方法。
 */
    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

}