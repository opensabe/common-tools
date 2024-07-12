package io.github.opensabe.spring.boot.starter.socketio.tracing.extend;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.corundumstudio.socketio.namespace.Namespace;
import com.corundumstudio.socketio.transport.NamespaceClient;
import io.github.opensabe.common.utils.SpringUtil;
import io.github.opensabe.spring.boot.starter.socketio.tracing.observation.ObservationService;
import io.github.opensabe.spring.boot.starter.socketio.tracing.observation.SocketIOExecuteDocumentation;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class NamespaceExtend extends Namespace   {

    public NamespaceExtend(String name, Configuration configuration) {
        super(name, configuration);
    }

    @Override
    public void onEvent(NamespaceClient client, String eventName, List<Object> args, AckRequest ackRequest) {
        MultiConsumer<NamespaceClient,String,List<Object>,AckRequest> multiConsumer = (n,s,l,a)->super.onEvent(n,s,l,a);
        SpringUtil.getBean(ObservationService.class).observationEvent(client, SocketIOExecuteDocumentation.SOCKET_EXECUTE_ON_EVENT, eventName, OnEvent.class.getName(),args,ackRequest,multiConsumer);
    }

    @Override
    public void onDisconnect(SocketIOClient client) {
        SpringUtil.getBean(ObservationService.class).observation(client, SocketIOExecuteDocumentation.SOCKET_EXECUTE_DISCONNECT, null, OnDisconnect.class.getName(),(t)-> super.onDisconnect(t));
    }

    @Override
    public void onConnect(SocketIOClient client) {
        SpringUtil.getBean(ObservationService.class).observation(client, SocketIOExecuteDocumentation.SOCKET_EXECUTE_CONNECT, null, OnConnect.class.getName(),(t)-> super.onConnect(t));
    }


}
