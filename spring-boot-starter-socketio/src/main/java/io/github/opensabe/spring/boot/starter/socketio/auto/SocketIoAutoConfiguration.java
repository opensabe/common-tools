package io.github.opensabe.spring.boot.starter.socketio.auto;

import io.github.opensabe.spring.boot.starter.socketio.conf.DefaultSocketIoHandlerConfiguration;
import io.github.opensabe.spring.boot.starter.socketio.conf.SocketIoConfiguration;
import io.github.opensabe.spring.boot.starter.socketio.configuration.MonitorConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

//@Configuration(proxyBeanMethods = false)
@AutoConfiguration
@Import({SocketIoConfiguration.class, DefaultSocketIoHandlerConfiguration.class, MonitorConfiguration.class})
public class SocketIoAutoConfiguration {
}