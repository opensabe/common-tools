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
