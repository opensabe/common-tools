package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

import java.util.Map;

public enum SocketIOExecuteDocumentation implements ObservationDocumentation, ObservationCov{

    SOCKET_EXECUTE_CONNECT {
        @Override
        public ObservationConvention getConvention() {
            return SocketIOExecuteObservationConnectConvention.DEFAULT;
        }

        @Override
        public String getName() {
            return "socketio.execute.connect";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return SocketIOExecuteObservationConnectConvention.class;
        }

    },

    SOCKET_EXECUTE_DISCONNECT {
        @Override
        public ObservationConvention getConvention() {
            return SocketIOExecuteObservationConnectConvention.DEFAULT;
        }
        @Override
        public String getName() {
            return "socketio.execute.disConnect";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return SocketIOExecuteObservationConnectConvention.class;
        }

    },
    SOCKET_EXECUTE_ON_EVENT {
        @Override
        public ObservationConvention getConvention() {
            return SocketIOExecuteObservationConvention.DEFAULT;
        }
        @Override
        public String getName() {
            return "socketio.execute.event";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return SocketIOExecuteObservationConvention.class;
        }
    };
    private static final Map<String,SocketIOExecuteDocumentation> map = Map.of(OnConnect.class.getName(),SOCKET_EXECUTE_CONNECT,
            OnDisconnect.class.getName(),SOCKET_EXECUTE_DISCONNECT, OnEvent.class.getName(),SOCKET_EXECUTE_ON_EVENT);

    public static SocketIOExecuteDocumentation get(String name){
        return map.get(name);
    }

}
