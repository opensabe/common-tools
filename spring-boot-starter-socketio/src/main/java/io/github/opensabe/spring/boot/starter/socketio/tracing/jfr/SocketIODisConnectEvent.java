package io.github.opensabe.spring.boot.starter.socketio.tracing.jfr;

import io.github.opensabe.spring.boot.starter.socketio.tracing.observation.SocketIOExecuteContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation","Socket-Disconnect"})
@Label("Disconnect Event")
@StackTrace(value = false)
public class SocketIODisConnectEvent extends Event {

    private String traceId;

    private String spanId;

    private String sessionId;

    private String headers;

    public SocketIODisConnectEvent(SocketIOExecuteContext context) {
        this.sessionId = context.getSocketIOClient().getSessionId().toString();
        this.headers = context.getAllHeaders();
    }
}