package io.github.opensabe.spring.boot.starter.socketio.tracing;

import java.util.Map;

public enum EventEnum {
    OnEvent,
    OnConnect,
    OnDisconnect;
    public static Map<String, EventEnum> map = Map.of(com.corundumstudio.socketio.annotation.OnConnect.class.getName(), OnConnect,
            com.corundumstudio.socketio.annotation.OnEvent.class.getName(), OnEvent,
            com.corundumstudio.socketio.annotation.OnDisconnect.class.getName(), OnDisconnect);
    public static EventEnum getInstance(String event){
        return map.get(event);
    }
}
