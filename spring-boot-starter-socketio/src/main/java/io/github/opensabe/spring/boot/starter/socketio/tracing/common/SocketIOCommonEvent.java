package io.github.opensabe.spring.boot.starter.socketio.tracing.common;

import com.corundumstudio.socketio.SocketIOClient;
import io.netty.handler.codec.http.HttpHeaders;

public interface SocketIOCommonEvent {
    SocketIOClient currentSocketIOClient();

    HttpHeaders currentHttpHeaders();
}
