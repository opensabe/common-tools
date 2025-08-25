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

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RetMsg extends MqMessage{
    private String body;
    private String topic;
    private PushType pushType;
    private Integer messageId;

    public RetMsg() {
    }

    public RetMsg(String body, String topic, PushType pushType, Integer messageId) {
        this.body = body;
        this.topic = topic;
        this.pushType = pushType;
        this.messageId = messageId;
    }

    public static void main(String[] args) {
        RetMsg retMsg = new RetMsg();
        retMsg.setBody("aaaa");
        retMsg.setMessageId(111);
        retMsg.setPushType(PushType.SPECIAL);
        String s = JsonUtil.toJSONString(retMsg);
        System.out.println(s);
        RetMsg retMsg1 = JSON.parseObject(s, RetMsg.class);
        System.out.println(retMsg1);
    }
}
