package io.github.opensabe.spring.boot.starter.socketio.configuration;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.boot.starter.socketio.tracing.jfr.ConnectionExecuteJFRGenerator;
import io.github.opensabe.spring.boot.starter.socketio.tracing.jfr.DisConnectExecuteJFRGenerator;
import io.github.opensabe.spring.boot.starter.socketio.tracing.jfr.OnEventExecuteJFRGenerator;
import io.github.opensabe.spring.boot.starter.socketio.tracing.observation.ObservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MonitorConfiguration {

    @Bean
    public ConnectionExecuteJFRGenerator connectionExecuteJFRGenerator () {
        return new ConnectionExecuteJFRGenerator();
    }
    @Bean
    public DisConnectExecuteJFRGenerator disConnectExecuteJFRGenerator () {
        return new DisConnectExecuteJFRGenerator();
    }
    @Bean
    public OnEventExecuteJFRGenerator onEventExecuteJFRGenerator () {
        return new OnEventExecuteJFRGenerator();
    }

    @Bean
    public ObservationService observationService(UnifiedObservationFactory unifiedObservationFactory) {
        return new ObservationService(unifiedObservationFactory);
    }
}
