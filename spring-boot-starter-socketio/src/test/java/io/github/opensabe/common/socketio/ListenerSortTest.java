package io.github.opensabe.common.socketio;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.annotation.OnEvent;
import io.github.opensabe.common.testcontainers.integration.SingleRedisIntegrationTest;
import io.socket.client.IO;
import io.socket.client.Socket;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.moditect.jfrunit.JfrEventTest;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author heng.ma
 */
@Log4j2
@Import(ListenerSortTest.Conf.class)
@SpringBootTest(classes = SocketIOStarter.App.class, properties = {
        "server.socketio.port=4002"
})
@JfrEventTest
@AutoConfigureObservability
@ExtendWith(SingleRedisIntegrationTest.class)
public class ListenerSortTest {

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        SingleRedisIntegrationTest.setProperties(registry);
    }

    public static List<String> list = new CopyOnWriteArrayList<>();
    
    @BeforeEach
    void setUp() {
        list.clear();
    }


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


    private static final String SERVER_URL = "ws://localhost:4002";
    private static final String HEADER_UID = "uid";
    private static final String USER_ID = "u1";

    @Test
    void test1() throws URISyntaxException, InterruptedException {


        IO.Options options = new IO.Options();
        options.extraHeaders = Map.of(HEADER_UID, List.of(USER_ID));
        Socket socket = IO.socket(SERVER_URL, options);
        CountDownLatch count = new CountDownLatch(1);
        socket.on(Socket.EVENT_CONNECT, o -> count.countDown());
        socket.connect();
        //这里服务器会收到两次connect事件，因此我们用event事件来测试

        Assertions.assertThat(count.await(5, TimeUnit.SECONDS))
                .withFailMessage(() -> "connect to "+SERVER_URL+" error")
                .isTrue();

        socket.emit("aa", ArrayUtils.toArray(3), ack -> {
            log.info("receive data {}", ack[0]);
        });

        Assertions.assertThat(list)
                .hasSize(3)
                .containsExactly("3","2","1");

        socket.close();
    }
}
