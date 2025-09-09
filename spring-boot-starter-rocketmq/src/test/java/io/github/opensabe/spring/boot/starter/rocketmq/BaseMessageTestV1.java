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
package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.utils.json.JsonUtil;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author heng.ma
 */
@DisplayName("RocketMQ基础消息测试")
public class BaseMessageTestV1 {

    public record User (String name, Integer age) {}

    @Test
    @DisplayName("测试V1消息，data为字符串")
    void testWithV1 () {
        MessageExt ext = new MessageExt();
        ext.setBody(SimpleMessageListener.jsonWrapper.getBytes());
        new SimpleMessageListener().onMessage(ext);
    }
    @Test
    @DisplayName("测试V2消息，data为对象")
    void testWithV2 () {
        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(SimpleMessageListener.json.getBytes());
        new SimpleMessageListener().onMessage(ext);
    }


    static class SimpleMessageListener extends AbstractMQConsumer {
        static final String jsonWrapper = "{\"data\" : \"{\\\"name\\\" : \\\"zhangsan\\\",\\\"age\\\": 10}\"}";
        static final String json = """
            {
                "data": {
                    "name" : "zhangsan",
                    "age": 10
                }
            }
        """;


        @Override
        public void onMessage(MessageExt ext) {
            onBaseMessage(convert(ext));
        }

        @Override
        protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
            User user = JsonUtil.parseObject(baseMQMessage.getData(), User.class);
            Assertions.assertNotNull(user);
            Assertions.assertEquals(10, user.age());
            Assertions.assertEquals("zhangsan", user.name());
        }
    }

}
