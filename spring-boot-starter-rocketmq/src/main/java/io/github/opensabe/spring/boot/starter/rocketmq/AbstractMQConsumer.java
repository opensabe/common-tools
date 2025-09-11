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

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.MessageExt;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.entity.base.vo.BaseMessage;

public abstract class AbstractMQConsumer extends AbstractConsumer<String> {

    protected abstract void onBaseMQMessage(BaseMQMessage baseMQMessage);

    @Override
    protected void onBaseMessage(BaseMessage<String> baseMessage) {
        onBaseMQMessage((BaseMQMessage) baseMessage);
    }

    @Override
    protected BaseMessage<String> convert(MessageExt ext) {
        String payload = new String(ext.getBody(), Charset.defaultCharset());
        // 先进行解码
        String decode = StringUtils.trim(MQMessageUtil.decode(payload));
        return convertV1(decode);
    }
}
