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

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.listener.MessageListener;

import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import com.corundumstudio.socketio.store.pubsub.PubSubMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubType;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RedissonPubSubStoreMessageListener<T> implements MessageListener<PubSubMessage> {
    final PubSubListener<T> listener;
    final SocketIoServerProperties socketIoServerProperties;
    private final Long nodeId;

    public RedissonPubSubStoreMessageListener(Long nodeId, PubSubListener<T> listener, PubSubType type, SocketIoServerProperties socketIoServerProperties) {
        this.nodeId = nodeId;
        this.listener = listener;
        this.socketIoServerProperties = socketIoServerProperties;
    }

    @Override
    public void onMessage(CharSequence channel, PubSubMessage msg) {
        try {
            if (msg instanceof DispatchMessage) {
                boolean match =
                        StringUtils.isBlank(socketIoServerProperties.getHealthCheckPacketName()) ||
                                StringUtils.equalsIgnoreCase(((DispatchMessage) msg).getPacket().getName(), socketIoServerProperties.getHealthCheckPacketName());
                if (match) {
                    SocketIoHealthCheck.lastDispatchMessage = System.currentTimeMillis();
                }

            }
            if (!nodeId.equals(msg.getNodeId())) {
                listener.onMessage((T) msg);
            }
        } catch (Throwable e) {
            log.error("RedissonPubSubStoreMessageListener-onMessage: error: {}", e.getMessage(), e);
        }
    }
}
