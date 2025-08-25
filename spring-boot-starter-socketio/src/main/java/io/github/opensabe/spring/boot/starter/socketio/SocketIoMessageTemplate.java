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
package io.github.opensabe.spring.boot.starter.socketio;


import com.corundumstudio.socketio.BroadcastAckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.protocol.Packet;

import java.net.URI;
import java.util.Collection;

public class SocketIoMessageTemplate {
    public static final String USER_ID_ROOM_PREFIX = "uid:";

    private final SocketIOServer server;

    public SocketIoMessageTemplate(SocketIOServer server) {
        this.server = server;
    }

    /**
     * 获取当前这个实例，这个用户有几个连接
     * @param uid 用户 id
     * @return
     */
    public Collection<SocketIOClient> getUserClients(String uid) {
        return this.server.getRoomOperations(USER_ID_ROOM_PREFIX + uid).getClients();
    }

    /**
     * 给某个用户发送消息
     * @param uid 用户 id
     * @param packet 包
     * @param ackCallback 广播回调
     * @param <T>
     */
    public <T> void sendToUser(String uid, Packet packet, BroadcastAckCallback<T> ackCallback) {
        this.server.getRoomOperations(USER_ID_ROOM_PREFIX + uid).send(packet, ackCallback);
    }

    public void sendEventToUser(String uid, String name, SocketIOClient excludedClient, Object... data) {
        this.server.getRoomOperations(USER_ID_ROOM_PREFIX + uid).sendEvent(name, excludedClient, data);
    }

    public void sendEventToUser(String uid, String name, Object... data) {
        this.server.getRoomOperations(USER_ID_ROOM_PREFIX + uid).sendEvent(name, data);
    }


    public <T> void sendEventToUser(String uid, String name, Object data, BroadcastAckCallback<T> ackCallback) {
        this.server.getRoomOperations(USER_ID_ROOM_PREFIX + uid).sendEvent(name, data, ackCallback);
    }

    public <T> void sendEventToUser(String uid, String name, Object data, SocketIOClient excludedClient, BroadcastAckCallback<T> ackCallback) {
        this.server.getRoomOperations(USER_ID_ROOM_PREFIX + uid).sendEvent(name, data, excludedClient, ackCallback);
    }

    /**
     * 获取当前这个主题有几个连接
     * @param topic 主题
     * @return
     */
    public Collection<SocketIOClient> getTopicClients(String topic) {
        return this.server.getRoomOperations(topic).getClients();
    }

    /**
     * 给某个主题发送消息
     * @param topic 主题
     * @param packet 包
     * @param ackCallback 广播回调
     * @param <T>
     */
    public <T> void sendToTopic(String topic, Packet packet, BroadcastAckCallback<T> ackCallback) {
        this.server.getRoomOperations(topic).send(packet, ackCallback);
    }

    public void sendEventToTopic(String topic, String name, SocketIOClient excludedClient, Object... data) {
        this.server.getRoomOperations(topic).sendEvent(name, excludedClient, data);
    }

    public void sendEventToTopic(String topic, String name, Object... data) {
        this.server.getRoomOperations(topic).sendEvent(name, data);
    }


    public <T> void sendEventToTopic(String topic, String name, Object data, BroadcastAckCallback<T> ackCallback) {
        this.server.getRoomOperations(topic).sendEvent(name, data, ackCallback);
    }

    public <T> void sendEventToTopic(String topic, String name, Object data, SocketIOClient excludedClient, BroadcastAckCallback<T> ackCallback) {
        this.server.getRoomOperations(topic).sendEvent(name, data, excludedClient, ackCallback);
    }
}
