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
package io.github.opensabe.spring.boot.starter.socketio.util;

import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.SocketIOClient;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import io.github.opensabe.spring.boot.starter.socketio.SocketIoMessageTemplate;
import lombok.extern.log4j.Log4j2;

/**
 * 收到退出登录的消息以后强行断开连接
 *
 * @author hengma
 * @time 2023/9/26 14:54
 */
@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + ForceDisconnectProducer.MQ_TOPIC_LOGOUT + "_SuperKickForceDisconnect",
        consumeMode = ConsumeMode.CONCURRENTLY,
        topic = ForceDisconnectProducer.MQ_TOPIC_LOGOUT,
        messageModel = MessageModel.BROADCASTING
)
public class LogoutConsumer extends AbstractMQConsumer {

    private final SocketIoMessageTemplate socketIoMessageTemplate;

    public LogoutConsumer(SocketIoMessageTemplate socketIoMessageTemplate) {
        this.socketIoMessageTemplate = socketIoMessageTemplate;
    }

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        var dto = JSONObject.parseObject(baseMQMessage.getData(), ForceDisconnectProducer.ForceDisconnectDTO.class);
        if (Objects.nonNull(dto) && StringUtils.isNotBlank(dto.getUserId())) {
            var clients = socketIoMessageTemplate.getUserClients(dto.getUserId());
            if (CollectionUtils.isNotEmpty(clients)) {
                log.info("LogoutConsumer.onBaseMQMessage find clients size {} by userId", clients.size());
                clients.forEach(SocketIOClient::disconnect);
            }
        }
    }
}

