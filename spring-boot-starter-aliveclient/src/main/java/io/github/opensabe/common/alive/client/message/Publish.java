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

import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Publish extends MqMessage {

    private Integer requestId;

    /**
     * 产品标识:表示是哪个产品
     */
    private Integer productCode;
    /**
     * id, remove duplicate
     */
    private String id;
    /**
     * 推送主题
     */
    private String topic;
    /**
     * 消息内容
     */
    private byte[] body;

    private PushType pushType;

    private String deviceId;

    private String accountId;

    private Long expiry;


}
