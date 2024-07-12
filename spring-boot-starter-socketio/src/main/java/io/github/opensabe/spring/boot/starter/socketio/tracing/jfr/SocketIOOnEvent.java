package io.github.opensabe.spring.boot.starter.socketio.tracing.jfr;

import com.corundumstudio.socketio.SocketIOClient;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation","Socket-OnEvent"})
@Label("On Event")
@StackTrace(value = false)
public class SocketIOOnEvent extends Event {

    private String traceId;

    private String spanId;

    private String sessionId;

    private String eventName;

    public SocketIOOnEvent(SocketIOClient socketIOClient,String eventName) {
        this.sessionId = socketIOClient.getSessionId().toString();
        this.eventName = eventName;
    }
}