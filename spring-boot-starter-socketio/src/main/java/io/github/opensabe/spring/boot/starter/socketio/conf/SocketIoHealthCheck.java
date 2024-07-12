package io.github.opensabe.spring.boot.starter.socketio.conf;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.TimeUnit;

@Log4j2
public class SocketIoHealthCheck {
    public static volatile long lastDispatchMessage = System.currentTimeMillis();

    public SocketIoHealthCheck() {
        new Thread(() -> {
            try {
                for (long i = 0; i >= 0; i++) {
                    long now = System.currentTimeMillis();
                    long interval = now - lastDispatchMessage;
                    if (interval > 5 * 60 * 1000L) {
                        log.error("SocketIoHealthCheck-init [health thread] [over 5m not receive dispatch message]");
                    }
                    TimeUnit.SECONDS.sleep(30L);
                }
            } catch (Throwable e) {
                log.error("SocketIoHealthCheck-init [health thread] error", e);
            }

        }).start();
    }
}
