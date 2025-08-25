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

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import lombok.extern.log4j.Log4j2;


@Log4j2
@RocketMQMessageListener(
        consumerGroup = "${spring.application.name}_" + ForceDisconnectProducer.MQ_TOPIC_FORCE_DISCONNECT + "_SuperKickForceDisconnect",
        consumeMode = ConsumeMode.CONCURRENTLY,
        topic = ForceDisconnectProducer.MQ_TOPIC_FORCE_DISCONNECT,
        messageModel = MessageModel.BROADCASTING,
        consumeThreadNumber = 64
)
public class ForceDisconnectConsumer extends AbstractMQConsumer {

    private SocketIOServer socketIOServer;

    public ForceDisconnectConsumer(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    @Override
    protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
        log.info("ForceDisconnectConsumer-onBaseMQMessage {}", JsonUtil.toJSONString(baseMQMessage));
        ForceDisconnectProducer.ForceDisconnectDTO forceDisconnectDTO = JSON.parseObject(baseMQMessage.getData(),
                ForceDisconnectProducer.ForceDisconnectDTO.class);
        Collection<SocketIOClient> clients = socketIOServer.getRoomOperations(forceDisconnectDTO.getRoomId()).getClients();
        for (SocketIOClient socketIOClient : clients) {
            UUID existSession = socketIOClient.getSessionId();
            UUID currentSession = forceDisconnectDTO.getSession();
            String userId = forceDisconnectDTO.getUserId();
            if (!Objects.equals(currentSession, existSession)) {
                log.info("ForceDisconnectConsumer-onBaseMQMessage [force disconnected] userId:{}, currentSession:{}, existSession:{}", userId,
                        currentSession, existSession);
                socketIOClient.disconnect();
            }
        }
    }
}
