package io.github.opensabe.spring.boot.starter.socketio.conf;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.corundumstudio.socketio.listener.DefaultExceptionListener;
import com.corundumstudio.socketio.listener.ExceptionListener;
import com.corundumstudio.socketio.store.StoreFactory;
import com.netflix.discovery.EurekaClient;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import io.github.opensabe.spring.boot.starter.rocketmq.MQProducer;
import io.github.opensabe.spring.boot.starter.socketio.SocketIoMessageTemplate;
import io.github.opensabe.spring.boot.starter.socketio.tracing.extend.NamespaceExtend;
import io.github.opensabe.spring.boot.starter.socketio.util.ForceDisconnectConsumer;
import io.github.opensabe.spring.boot.starter.socketio.util.ForceDisconnectProducer;
import io.github.opensabe.spring.boot.starter.socketio.util.SocketConnectionUtil;
import io.netty.channel.epoll.Epoll;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SocketIoServerProperties.class)
public class SocketIoConfiguration {

//    @Bean
//    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer) {
//        return new SpringAnnotationScanner(socketIOServer);
//    }



    @Bean
    @ConditionalOnMissingBean
    public OrderedSpringAnnotationScanner springAnnotationScanner() {
        return new OrderedSpringAnnotationScanner();
    }

    @Bean
    @ConditionalOnMissingBean
    public ListenerAdder listenerAdder (SocketIOServer server, OrderedSpringAnnotationScanner scanner) {
        return new ListenerAdder(server, scanner);
    }

    public static class ListenerAdder implements InitializingBean, Ordered {
        private final OrderedSpringAnnotationScanner scanner;
        private final SocketIOServer server;
        public ListenerAdder(SocketIOServer server,OrderedSpringAnnotationScanner scanner) {
            this.server = server;
            this.scanner = scanner;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            List<Object> list = new ArrayList<>(scanner.listeners.size());
            list.addAll(scanner.listeners);
            //排序
            AnnotationAwareOrderComparator.sort(list);
            list.forEach(l -> {
                log.info("{} bean listeners added", l.getClass());
                server.addListeners(l, l.getClass());
            });
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    public static class OrderedSpringAnnotationScanner implements BeanPostProcessor {

        private final List<Class<? extends Annotation>> annotations =
                Arrays.asList(OnConnect.class, OnDisconnect.class, OnEvent.class);

        private final Set<Object> listeners = new HashSet<>();


        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            AtomicBoolean add = new AtomicBoolean();
            ReflectionUtils.doWithMethods(bean.getClass(),
                    method -> add.set(true),
                    method -> {
                        for (Class<? extends Annotation> annotationClass : annotations) {
                            if (method.isAnnotationPresent(annotationClass)) {
                                return true;
                            }
                        }
                        return false;
                    });

            if (add.get()) {
                listeners.add(bean);
            }

            return bean;
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ExceptionListener exceptionListener() {
        return new DefaultExceptionListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public SocketIOServer server(SocketIoServerProperties socketIoServerProperties, StoreFactory storeFactory, ExceptionListener exceptionListener) {
        socketIoServerProperties.setStoreFactory(storeFactory);
        socketIoServerProperties.setExceptionListener(exceptionListener);

        if (socketIoServerProperties.isUseLinuxNativeEpoll()
                && !Epoll.isAvailable()) {
            log.warn("SocketIoConfiguration-server: Epoll library not available, disabling native epoll");
            socketIoServerProperties.setUseLinuxNativeEpoll(false);
        }

        // config specific namespace
        socketIoServerProperties.setDefaultNamespace(new NamespaceExtend(NamespaceExtend.DEFAULT_NAME,socketIoServerProperties.cloneForNamespace()));

        SocketIOServer socketIOServer = new SocketIOServer(socketIoServerProperties);

        return socketIOServer;
    }

    @Bean
    public SocketIoMessageTemplate socketIoMessageTemplate(SocketIOServer server) {
        return new SocketIoMessageTemplate(server);
    }

    @Bean
    public SocketIOServerLifecycle serverLifecycle(SocketIOServer socketIOServer) {
        return new SocketIOServerLifecycle(socketIOServer);
    }

    @Bean
    public AttributedSocketIoClientFactory attributedSocketIoClientFactory() {
        return new AttributedSocketIoClientFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedissonStoreFactory redissonStoreFactory(RedissonClient redissonClient, SocketIoServerProperties serverProperties) {
        return new RedissonStoreFactory(redissonClient, serverProperties);
    }

    @Bean
    @ConditionalOnClass(EurekaClient.class)
    public SocketIoEurekaMetadataModifier socketIoEurekaMetadataModifier(SocketIoServerProperties serverProperties) {
        return new SocketIoEurekaMetadataModifier(serverProperties);
    }

    /**
     * 通过 SmartLifecycle 实现对于优雅关闭的兼容
     */
    @Log4j2
    public static class SocketIOServerLifecycle implements SmartLifecycle {

        private final SocketIOServer server;

        private volatile boolean running = false;

        public SocketIOServerLifecycle(SocketIOServer server) {
            this.server = server;
        }

        @Override
        public void start() {
            try {
                server.start();
            } catch (Throwable e) {
                log.fatal("SocketIOServerLifecycle-start failed, {}", e.getMessage(), e);
            }
            running = true;
        }

        @Override
        public void stop() {
            try {
                server.stop();
            } catch (Throwable e) {
                log.fatal("SocketIOServerLifecycle-stop failed, {}", e.getMessage(), e);
            }
            running = false;
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }

    @Bean
    @ConditionalOnBean(MQProducer.class)
    public ForceDisconnectProducer forceDisconnectProducer(MQProducer mqProducer) {
        return new ForceDisconnectProducer(mqProducer);
    }

    @Bean
    @ConditionalOnClass(AbstractMQConsumer.class)
    @ConditionalOnBean(MQProducer.class)
    public ForceDisconnectConsumer forceDisconnectConsumer(SocketIOServer socketIOServer) {
        return new ForceDisconnectConsumer(socketIOServer);
    }

    @Bean
    @ConditionalOnBean(ForceDisconnectProducer.class)
    public SocketConnectionUtil socketConnectionUtil(ForceDisconnectProducer forceDisconnectProducer) {

        return new SocketConnectionUtil(forceDisconnectProducer);
    }

    @Bean
    public SocketIoHealthCheck socketIoHealthCheck() {

        return new SocketIoHealthCheck();
    }

}
