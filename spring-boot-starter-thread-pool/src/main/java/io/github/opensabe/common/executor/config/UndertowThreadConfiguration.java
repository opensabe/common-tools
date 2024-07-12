package io.github.opensabe.common.executor.config;

import io.github.opensabe.common.executor.ThreadPoolFactory;
import io.github.opensabe.common.executor.ThreadPoolFactoryGracefulShutDownHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@ConditionalOnClass(name = "io.undertow.Undertow")
@Configuration(proxyBeanMethods = false)
public class UndertowThreadConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolFactoryGracefulShutDownHandler threadPoolFactoryGracefulShutDownHandler(ThreadPoolFactory threadPoolFactory) {
        return new ThreadPoolFactoryGracefulShutDownHandler(threadPoolFactory);
    }
}
