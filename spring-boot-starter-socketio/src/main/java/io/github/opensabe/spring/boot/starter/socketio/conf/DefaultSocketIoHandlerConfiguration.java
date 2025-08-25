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
package io.github.opensabe.spring.boot.starter.socketio.conf;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import io.github.opensabe.base.code.BizCodeEnum;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.socketio.AttributedSocketIoClient;
import io.github.opensabe.spring.boot.starter.socketio.BaseAck;
import io.github.opensabe.spring.boot.starter.socketio.SocketIoMessageTemplate;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.UUID;

/**
 * 设置order为HIGHEST_PRECEDENCE + 1000
 */
@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE+1000)
@Configuration(proxyBeanMethods = false)
public class DefaultSocketIoHandlerConfiguration {
    @Autowired
    private AttributedSocketIoClientFactory attributedSocketIoClientFactory;

    /**
     * 连接建立的时候要做的事情
     * @param client
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        AttributedSocketIoClient attributedSocketIoClient = attributedSocketIoClientFactory.addSocketIoClient(client);
        UUID socketSessionId = client.getSessionId();
        String ip = client.getRemoteAddress().toString();
        HandshakeData handshakeData = client.getHandshakeData();
        if (handshakeData != null) {
            HttpHeaders httpHeaders = handshakeData.getHttpHeaders();
            if (httpHeaders != null) {
                String uid = httpHeaders.get("uid");
                if (StringUtils.isNotBlank(uid)) {
                    //加入自己的 room
                    client.joinRoom(SocketIoMessageTemplate.USER_ID_ROOM_PREFIX + uid);
                    attributedSocketIoClient.setUserId(uid);
                }
                var operId = httpHeaders.get("operId");
                if (StringUtils.isNotBlank(operId)) {
                    attributedSocketIoClient.setOperId(operId);
                }
            }
        }
        log.info("DefaultSocketIoHandlerConfiguration-onConnect, socketSessionId:{}, ip:{}, headers: {}", socketSessionId, ip, client.getHandshakeData().getHttpHeaders().toString());
    }

    /**
     * 订阅，其实就是进入某个房间
     * @param client
     * @param request
     * @param topic
     */
    @OnEvent("sub")
    public void sub(SocketIOClient client, AckRequest request, String topic) {
        client.joinRoom(topic);
        log.info("DefaultSocketIoHandlerConfiguration-sub: client.id: {}, topic {}, allTopics: {}", client.getSessionId(), topic, JsonUtil.toJSONString(client.getAllRooms()));
        //需要发送确认，这个由客户端决定是否处理
        request.sendAckData(BaseAck.builder().b(BizCodeEnum.SUCCESS.getVal()).build());
    }

    @OnEvent("unsub")
    public void unsub(SocketIOClient client, AckRequest request, String topic) {
        client.leaveRoom(topic);
        log.info("DefaultSocketIoHandlerConfiguration-unsub: client.id: {}, topic {}, allTopics: {}", client.getSessionId(), topic, JsonUtil.toJSONString(client.getAllRooms()));
        //需要发送确认，这个由客户端决定是否处理
        request.sendAckData(BaseAck.builder().b(BizCodeEnum.SUCCESS.getVal()).build());
    }
}
