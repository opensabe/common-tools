package io.github.opensabe.common.socketio;

import io.github.opensabe.spring.boot.starter.socketio.CommonAttribute;
import io.github.opensabe.spring.boot.starter.socketio.conf.SocketIoServerProperties;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


@Log4j2
public class SocketIOTest extends SocketIOStarter {
    private static final String URL = "http://localhost:";
    @Autowired
    private SocketIoServerProperties socketIoServerProperties;

    /**
     * socketio测试地址 https://amritb.github.io/socketio-client-tool/v1/#url=d3M6Ly9sb2NhbGhvc3Q6NDAwMQ==&path=L3NvY2tldC5pbw==&opt=&events=
     * url ws://localhost:4001/socket.io
     * event sub
     * @throws InterruptedException
     */
    @Test
    public void testClient() throws InterruptedException, URISyntaxException {
        TimeUnit.SECONDS.sleep(1);
        connectToLocalhost();
    }

    public void connectToLocalhost() throws InterruptedException, URISyntaxException {
        IO.Options opt = createOptionsWithQueryParams();
        String url = URL + socketIoServerProperties.getPort();
        final BlockingQueue<Object> values = new LinkedBlockingQueue<>();
        Socket socket = IO.socket(url,opt);
        socket.on(Socket.EVENT_CONNECT, objects -> {
            System.out.println("connect SocketIOTest-connectToLocalhost ");
            socket.emit("sub", "hello");
        })
        ;
        socket.connect();

        values.poll(2,TimeUnit.SECONDS);
        socket.close();
    }

    public IO.Options createOptionsWithQueryParams() {
        Map<String, List<String>> extraHeaders = new HashMap<>();
        extraHeaders.put(CommonAttribute.UID, List.of( "21131221uid123213213"));
        extraHeaders.put(CommonAttribute.PLATFORM, List.of( "android"));
        extraHeaders.put(CommonAttribute.OPERATOR_ID, List.of( "2"));

        IO.Options options = new IO.Options();
        options.extraHeaders = extraHeaders;
        return options;
    }
}