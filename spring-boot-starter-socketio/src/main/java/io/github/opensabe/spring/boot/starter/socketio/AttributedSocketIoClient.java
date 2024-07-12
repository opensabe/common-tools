package io.github.opensabe.spring.boot.starter.socketio;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.protocol.EngineIOVersion;
import com.corundumstudio.socketio.protocol.Packet;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Log4j2
public class AttributedSocketIoClient implements SocketIOClient {
    private final SocketIOClient delegate;
    private final Map<String, Object> attributes = Maps.newConcurrentMap();

    public AttributedSocketIoClient(SocketIOClient delegate) {
        this.delegate = delegate;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    public String getUserId() {
        return (String) getAttribute(CommonAttribute.UID);
    }

    public void setUserId(String userId) {
        setAttribute(CommonAttribute.UID, userId);
    }

    public void setOperId (String operId) {
        setAttribute(CommonAttribute.OPERATOR_ID, operId);
    }

    public String getOperId () {
        return (String) getAttribute(CommonAttribute.OPERATOR_ID);
    }

    @Override
    public HandshakeData getHandshakeData() {
        return delegate.getHandshakeData();
    }

    @Override
    public Transport getTransport() {
        return delegate.getTransport();
    }

    @Override
    public EngineIOVersion getEngineIOVersion() {
        return delegate.getEngineIOVersion();
    }

    @Override
    public boolean isWritable() {
        return delegate.isWritable();
    }

    @Override
    public void sendEvent(String name, AckCallback<?> ackCallback, Object... data) {
        delegate.sendEvent(name, ackCallback, data);
    }

    @Override
    public void send(Packet packet, AckCallback<?> ackCallback) {
        delegate.send(packet, ackCallback);
    }

    @Override
    public SocketIONamespace getNamespace() {
        return delegate.getNamespace();
    }

    @Override
    public UUID getSessionId() {
        return delegate.getSessionId();
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return delegate.getRemoteAddress();
    }

    @Override
    public boolean isChannelOpen() {
        return delegate.isChannelOpen();
    }

    @Override
    public void joinRoom(String room) {
        delegate.joinRoom(room);
    }

    @Override
    public void joinRooms(Set<String> set) {
        delegate.joinRooms(set);
    }

    @Override
    public void leaveRoom(String room) {
        delegate.leaveRoom(room);
    }

    @Override
    public void leaveRooms(Set<String> set) {
        delegate.leaveRooms(set);
    }

    @Override
    public Set<String> getAllRooms() {
        return delegate.getAllRooms();
    }

    @Override
    public int getCurrentRoomSize(String s) {
        return delegate.getCurrentRoomSize(s);
    }

    @Override
    public void send(Packet packet) {
        delegate.send(packet);
    }

    @Override
    public void disconnect() {
        delegate.disconnect();
    }

    @Override
    public void sendEvent(String name, Object... data) {
        delegate.sendEvent(name, data);
    }

    @Override
    public void set(String key, Object val) {
        delegate.set(key, val);
    }

    @Override
    public <T> T get(String key) {
        return delegate.get(key);
    }

    @Override
    public boolean has(String key) {
        return delegate.has(key);
    }

    @Override
    public void del(String key) {
        delegate.del(key);
    }
}
