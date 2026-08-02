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

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.Transport;
import com.corundumstudio.socketio.protocol.EngineIOVersion;
import com.corundumstudio.socketio.protocol.Packet;
import com.google.common.collect.Maps;

import lombok.extern.log4j.Log4j2;

/**
 * AttributedSocketIoClient。
 */
@Log4j2
public class AttributedSocketIoClient implements SocketIOClient {
/** 被装饰的 QueryConditional。 */
    private final SocketIOClient delegate;
    private final Map<String, Object> attributes = Maps.newConcurrentMap();

    public AttributedSocketIoClient(SocketIOClient delegate) {
        this.delegate = delegate;
    }

    /**
     * @param attribute 待设置值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * @return attribute
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /** @return 是否包含Attribute */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * @return userId
     */
    public String getUserId() {
        return (String) getAttribute(CommonAttribute.UID);
    }

    /**
     * @param userId 待设置值
     */
    public void setUserId(String userId) {
        setAttribute(CommonAttribute.UID, userId);
    }

    /**
     * @return operId
     */
    public String getOperId() {
        return (String) getAttribute(CommonAttribute.OPERATOR_ID);
    }

    /**
     * @param operId 待设置值
     */
    public void setOperId(String operId) {
        setAttribute(CommonAttribute.OPERATOR_ID, operId);
    }

    /** {@inheritDoc} */
    @Override
    public HandshakeData getHandshakeData() {
        return delegate.getHandshakeData();
    }

    /** {@inheritDoc} */
    @Override
    public Transport getTransport() {
        return delegate.getTransport();
    }

    /** {@inheritDoc} */
    @Override
    public EngineIOVersion getEngineIOVersion() {
        return delegate.getEngineIOVersion();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWritable() {
        return delegate.isWritable();
    }

    /** {@inheritDoc} */
    @Override
    public void sendEvent(String name, AckCallback<?> ackCallback, Object... data) {
        delegate.sendEvent(name, ackCallback, data);
    }

    /** {@inheritDoc} */
    @Override
    public void send(Packet packet, AckCallback<?> ackCallback) {
        delegate.send(packet, ackCallback);
    }

    /** {@inheritDoc} */
    @Override
    public SocketIONamespace getNamespace() {
        return delegate.getNamespace();
    }

    /** {@inheritDoc} */
    @Override
    public UUID getSessionId() {
        return delegate.getSessionId();
    }

    /** {@inheritDoc} */
    @Override
    public SocketAddress getRemoteAddress() {
        return delegate.getRemoteAddress();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isChannelOpen() {
        return delegate.isChannelOpen();
    }

    /** {@inheritDoc} */
    @Override
    public void joinRoom(String room) {
        delegate.joinRoom(room);
    }

    /** {@inheritDoc} */
    @Override
    public void joinRooms(Set<String> set) {
        delegate.joinRooms(set);
    }

    /** {@inheritDoc} */
    @Override
    public void leaveRoom(String room) {
        delegate.leaveRoom(room);
    }

    /** {@inheritDoc} */
    @Override
    public void leaveRooms(Set<String> set) {
        delegate.leaveRooms(set);
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getAllRooms() {
        return delegate.getAllRooms();
    }

    /** {@inheritDoc} */
    @Override
    public int getCurrentRoomSize(String s) {
        return delegate.getCurrentRoomSize(s);
    }

    /** {@inheritDoc} */
    @Override
    public void send(Packet packet) {
        delegate.send(packet);
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect() {
        delegate.disconnect();
    }

    /** {@inheritDoc} */
    @Override
    public void sendEvent(String name, Object... data) {
        delegate.sendEvent(name, data);
    }

    /** {@inheritDoc} */
    @Override
    public void set(String key, Object val) {
        delegate.set(key, val);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T get(String key) {
        return delegate.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean has(String key) {
        return delegate.has(key);
    }

    /** {@inheritDoc} */
    @Override
    public void del(String key) {
        delegate.del(key);
    }
}
