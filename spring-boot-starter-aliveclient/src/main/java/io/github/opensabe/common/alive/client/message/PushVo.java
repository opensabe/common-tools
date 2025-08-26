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

public class PushVo {

    private static final ThreadLocal<AtomicInteger> requestIdThreadLocal = ThreadLocal.withInitial(() -> new AtomicInteger());

    public final String topic;
    public final String deviceId;
    public final PushType pushType;
    public final String accountId;
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
                throw new NullPointerException("单推消息时,deviceId不能为空");
            } else if (pushType == PushType.MULTI && StringUtils.isBlank(accountId)) {
                throw new NullPointerException("组推消息时,accountId不能为空");
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

    public static int generateRequestId() {
        if (requestIdThreadLocal.get().intValue() < 0) {
            requestIdThreadLocal.remove();
        }
        return requestIdThreadLocal.get().incrementAndGet();
    }

    public int getRequestId() {
        return this.requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

}