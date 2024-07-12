package io.github.opensabe.spring.cloud.parent.web.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.spring.cloud.parent.web.common.handler.GenericHttpMessageConverterSecretCheckPostProcessor;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerJFRProperties;
import io.github.opensabe.spring.cloud.parent.web.common.jfr.HttpServerRequestObservationToJFRGenerator;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.DefaultWebServerFactoryCustomizer;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.UndertowGracefulShutdownHandler;
import io.github.opensabe.spring.cloud.parent.web.common.undertow.UndertowGracefulShutdownInitializer;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.undertow.ConfigurableUndertowWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({HttpServerJFRProperties.class})
public class WebServerConfiguration {
    @Bean
    public WebServerFactoryCustomizer<ConfigurableUndertowWebServerFactory> undertowWebServerAccessLogTimingEnabler(ServerProperties serverProperties) {
        return new DefaultWebServerFactoryCustomizer(serverProperties);
    }


    @Bean
    //仅仅是为了保证项目中至少有一个 UndertowGracefulShutdownHandler
    public UndertowGracefulShutdownHandler defaultUndertowGracefulShutdownHandler() {
        return new UndertowGracefulShutdownHandler() {
            @Override
            public int getOrder() {
                return Ordered.LOWEST_PRECEDENCE;
            }

            @Override
            public void gracefullyShutdown() {
                // do nothing
            }
        };
    }

    @Bean
    public UndertowGracefulShutdownInitializer undertowGracefulShutdownInitializer(List<UndertowGracefulShutdownHandler> undertowGracefulShutdownHandlers) {
        return new UndertowGracefulShutdownInitializer(undertowGracefulShutdownHandlers);
    }

    @Bean
    public HttpServerRequestObservationToJFRGenerator httpServerRequestObservationToJFRGenerator(HttpServerJFRProperties properties) {
        return new HttpServerRequestObservationToJFRGenerator(properties);
    }

    @Bean
    public GenericHttpMessageConverterSecretCheckPostProcessor genericHttpMessageConverterSecretCheckPostProcessor(GlobalSecretManager globalSecretManager) {
        return new GenericHttpMessageConverterSecretCheckPostProcessor(globalSecretManager);
    }

    //这里原来有 JFRFilter，用于通过 JFR 记录每个 HTTP 请求的详细信息
    //现在已经不需要了，因为我们已经将 servlet 线程池替换成自己的 Factory 封装的线程池，而这个线程池已经会记录每个请求
    //并且，更好的方式应该是通过 micrometer 的 Observation 去记录每个请求的详细信息，之后消费 Observation 生成 JFR 事件
    //参考：org.springframework.web.filter.ServerHttpObservationFilter
}
