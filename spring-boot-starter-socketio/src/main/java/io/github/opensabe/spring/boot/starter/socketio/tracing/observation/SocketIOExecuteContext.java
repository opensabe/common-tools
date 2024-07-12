package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import com.corundumstudio.socketio.SocketIOClient;
import io.github.opensabe.spring.boot.starter.socketio.tracing.EventEnum;
import io.github.opensabe.spring.boot.starter.socketio.tracing.common.SocketIOParam;
import io.micrometer.observation.Observation;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocketIOExecuteContext extends Observation.Context implements SocketIOParam {

    private SocketIOClient socketIOClient;
    private String eventName;
    private EventEnum eventEnum;
    public SocketIOExecuteContext(SocketIOClient socketIOClient,String eventName,EventEnum eventEnum) {
        this.socketIOClient = socketIOClient;
        this.eventName = eventName;
        this.eventEnum = eventEnum;
    }

    @Override
    public SocketIOClient currentSocketIOClient() {
        return socketIOClient;
    }

    @Override
    public HttpHeaders currentHttpHeaders() {
        return socketIOClient.getHandshakeData().getHttpHeaders();
    }
}
