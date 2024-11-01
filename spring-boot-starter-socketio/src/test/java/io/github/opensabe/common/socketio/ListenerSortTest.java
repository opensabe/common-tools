package io.github.opensabe.common.socketio;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author heng.ma
 */
@Log4j2
@Import(ListenerSortTest.Conf.class)
public class ListenerSortTest extends SocketIOStarter {


    public static List<String> list = new CopyOnWriteArrayList<>();


    public static class Conf {

        @Bean
        public Listener1 listener1 () {
            return new Listener1();
        }
        @Bean
        public Listener2 listener2 () {
            return new Listener2();
        }
        @Bean
        public Listener3 listener3 () {
            return new Listener3();
        }
    }

    @Order(3)
    public static class Listener1  {

        @OnEvent("aa")
        public void onEvent (SocketIOClient client, AckRequest request, String a) {
            log.info("listener1 receive event aa data {}", a);
            list.add("1");
        }
    }

    @Order(2)
    public static class Listener2  {

        @OnEvent("aa")
        public void onEvent (SocketIOClient client, AckRequest request, String a) {
            log.info("listener2 receive event aa data {}", a);
            list.add("2");
        }
    }
    @Order(1)
    public static class Listener3  {

        @OnEvent("aa")
        public void onEvent (SocketIOClient client, AckRequest request, String a) {
            log.info("listener3 receive event aa data {}", a);
            list.add("3");
        }
    }


    @Test
    void test1 () throws URISyntaxException, InterruptedException {
        IO.Options options = new IO.Options();
        options.extraHeaders = Map.of("uid", List.of("u1"));
        Socket socket = IO.socket("ws://localhost:4001", options);

        //这里服务器会收到两次connect事件，因此我们用event事件来测试
        if (!socket.connected()) {
            socket.connect();
        }

        socket.emit("aa", ArrayUtils.toArray(3), ack -> {
            log.info("receive data {}", ack[0]);
        });

        Thread.sleep(1000L);

        Assertions.assertThat(list)
                .hasSize(3)
                .containsExactly("3","2","1");

        socket.close();
    }
}
