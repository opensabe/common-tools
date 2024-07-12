package io.github.opensabe.spring.boot.starter.socketio.conf;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration(proxyBeanMethods = false)
public class EventExceptionConfiguration {

    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public EventExceptionAop exceptionAop() {
        return new EventExceptionAop(applicationContext);
    }

}
