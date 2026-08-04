/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.boot.starter.socketio.conf;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.redisson.api.RedissonClient;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ReflectionUtils;

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

/**
 * Socket.IO 服务端 Spring 配置。
 * <p>
 * 注册 {@link SocketIOServer}、注解扫描、Redisson 会话存储、Eureka 元数据、
 * 强制断连 MQ 集成及健康检查等 Bean。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SocketIoServerProperties.class)
public class SocketIoConfiguration {

//    @Bean
//    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIOServer) {
//        return new SpringAnnotationScanner(socketIOServer);
//    }

    /**
     * 按 {@link org.springframework.core.annotation.Order} 收集 Socket.IO 事件监听 Bean 的后处理器。
     *
     * @return 有序注解扫描器
     */
    @Bean
    @ConditionalOnMissingBean
    public OrderedSpringAnnotationScanner springAnnotationScanner() {
        return new OrderedSpringAnnotationScanner();
    }

    /**
     * 在应用启动完成后将扫描到的监听器注册到 {@link SocketIOServer}。
     *
     * @param server  Socket.IO 服务端
     * @param scanner 注解扫描器
     * @return 监听器注册器
     */
    @Bean
    @ConditionalOnMissingBean
    public ListenerAdder listenerAdder(SocketIOServer server, OrderedSpringAnnotationScanner scanner) {
        return new ListenerAdder(server, scanner);
    }

    /**
     * 注册 Socket.IO 默认异常监听器。
     *
     * @return 异常监听器
     */
    @Bean
    @ConditionalOnMissingBean
    public ExceptionListener exceptionListener() {
        return new DefaultExceptionListener();
    }

    /**
     * 创建并配置 {@link SocketIOServer}，含 Epoll 可用性回退与默认命名空间扩展。
     *
     * @param socketIoServerProperties 服务端配置
     * @param storeFactory             会话存储工厂
     * @param exceptionListener        异常监听器
     * @return Socket.IO 服务端实例
     */
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

        socketIoServerProperties.setDefaultNamespace(new NamespaceExtend(NamespaceExtend.DEFAULT_NAME, socketIoServerProperties.cloneForNamespace()));

        SocketIOServer socketIOServer = new SocketIOServer(socketIoServerProperties);

        return socketIOServer;
    }

    /**
     * 注册 Socket.IO 消息发送模板。
     *
     * @param server Socket.IO 服务端
     * @return 消息模板
     */
    @Bean
    public SocketIoMessageTemplate socketIoMessageTemplate(SocketIOServer server) {
        return new SocketIoMessageTemplate(server);
    }

    /**
     * 注册通过 {@link SmartLifecycle} 管理启停的 Socket.IO 生命周期 Bean。
     *
     * @param socketIOServer Socket.IO 服务端
     * @return 生命周期管理器
     */
    @Bean
    public SocketIOServerLifecycle serverLifecycle(SocketIOServer socketIOServer) {
        return new SocketIOServerLifecycle(socketIOServer);
    }

    /**
     * 注册带属性的 Socket.IO 客户端工厂。
     *
     * @return 客户端工厂
     */
    @Bean
    public AttributedSocketIoClientFactory attributedSocketIoClientFactory() {
        return new AttributedSocketIoClientFactory();
    }

    /**
     * 注册基于 Redisson 的 Socket.IO 会话存储工厂。
     *
     * @param redissonClient   Redisson 客户端
     * @param serverProperties 服务端配置
     * @return Redisson 存储工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public RedissonStoreFactory redissonStoreFactory(RedissonClient redissonClient, SocketIoServerProperties serverProperties) {
        return new RedissonStoreFactory(redissonClient, serverProperties);
    }

    /**
     * 在 Eureka 可用时注册 Socket.IO 端口元数据修改器。
     *
     * @param serverProperties 服务端配置
     * @return Eureka 元数据修改器
     */
    @Bean
    @ConditionalOnClass(EurekaClient.class)
    public SocketIoEurekaMetadataModifier socketIoEurekaMetadataModifier(SocketIoServerProperties serverProperties) {
        return new SocketIoEurekaMetadataModifier(serverProperties);
    }

    /**
     * 在存在 {@link MQProducer} 时注册强制断连消息生产者。
     *
     * @param mqProducer MQ 生产者
     * @return 强制断连生产者
     */
    @Bean
    @ConditionalOnBean(MQProducer.class)
    public ForceDisconnectProducer forceDisconnectProducer(MQProducer mqProducer) {
        return new ForceDisconnectProducer(mqProducer);
    }

    /**
     * 在 MQ 与消费者类均可用时注册强制断连消息消费者。
     *
     * @param socketIOServer Socket.IO 服务端
     * @return 强制断连消费者
     */
    @Bean
    @ConditionalOnClass(AbstractMQConsumer.class)
    @ConditionalOnBean(MQProducer.class)
    public ForceDisconnectConsumer forceDisconnectConsumer(SocketIOServer socketIOServer) {
        return new ForceDisconnectConsumer(socketIOServer);
    }

    /**
     * 在强制断连生产者存在时注册连接管理工具。
     *
     * @param forceDisconnectProducer 强制断连生产者
     * @return 连接管理工具
     */
    @Bean
    @ConditionalOnBean(ForceDisconnectProducer.class)
    public SocketConnectionUtil socketConnectionUtil(ForceDisconnectProducer forceDisconnectProducer) {
        return new SocketConnectionUtil(forceDisconnectProducer);
    }

    /**
     * 注册 Socket.IO 健康检查组件。
     *
     * @return 健康检查
     */
    @Bean
    public SocketIoHealthCheck socketIoHealthCheck() {
        return new SocketIoHealthCheck();
    }

    /**
     * 应用启动完成后，按 Order 将扫描到的监听器 Bean 注册到 Socket.IO 服务端。
     */
    public static class ListenerAdder implements ApplicationListener<ApplicationStartedEvent> {

        /**
         * Socket.IO 注解扫描器。
         */
        private final OrderedSpringAnnotationScanner scanner;

        /**
         * 托管的 Socket.IO 服务端。
         */
        private final SocketIOServer server;

        /**
         * @param server  Socket.IO 服务端
         * @param scanner 注解扫描器
         */
        public ListenerAdder(SocketIOServer server, OrderedSpringAnnotationScanner scanner) {
            this.server = server;
            this.scanner = scanner;
        }

        /**
         * 按 {@link AnnotationAwareOrderComparator} 排序后批量注册监听器。
         *
         * @param event 应用已启动事件
         */
        @Override
        public void onApplicationEvent(ApplicationStartedEvent event) {
            List<Object> list = new ArrayList<>(scanner.listeners);
            AnnotationAwareOrderComparator.sort(list);
            list.forEach(l -> {
                server.addListeners(l, l.getClass());
                log.info("{} bean listeners added", l.getClass());
            });
        }
    }

    /**
     * 扫描 Spring Bean 上 {@link OnConnect}、{@link OnDisconnect}、{@link OnEvent} 注解并收集为监听器。
     */
    public static class OrderedSpringAnnotationScanner implements BeanPostProcessor {

        /**
         * 需要识别的 Socket.IO 事件注解类型。
         */
        private final List<Class<? extends Annotation>> annotations =
                Arrays.asList(OnConnect.class, OnDisconnect.class, OnEvent.class);

        /**
         * 已识别为 Socket.IO 监听器的 Bean 集合。
         */
        private final Set<Object> listeners = new HashSet<>();

        /**
         * Bean 初始化完成后，若目标类方法上存在 Socket.IO 事件注解则加入监听器集合。
         *
         * @param bean     Bean 实例
         * @param beanName Bean 名称
         * @return 原 Bean 实例
         * @throws BeansException Bean 处理异常
         */
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            AtomicBoolean add = new AtomicBoolean();
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            ReflectionUtils.doWithMethods(targetClass,
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

    /**
     * 通过 {@link SmartLifecycle} 实现 Socket.IO 服务端启停，以兼容应用优雅关闭。
     */
    @Log4j2
    public static class SocketIOServerLifecycle implements SmartLifecycle {

        /**
         * 托管的 Socket.IO 服务端。
         */
        private final SocketIOServer server;

        /**
         * 当前是否处于运行状态（由本生命周期维护，非服务端真实状态）。
         */
        private volatile boolean running = false;

        /**
         * @param server Socket.IO 服务端
         */
        public SocketIOServerLifecycle(SocketIOServer server) {
            this.server = server;
        }

        /**
         * 启动 Socket.IO 服务端。
         */
        @Override
        public void start() {
            try {
                server.start();
            } catch (Throwable e) {
                log.fatal("SocketIOServerLifecycle-start failed, {}", e.getMessage(), e);
            }
            running = true;
        }

        /**
         * 停止 Socket.IO 服务端。
         */
        @Override
        public void stop() {
            try {
                server.stop();
            } catch (Throwable e) {
                log.fatal("SocketIOServerLifecycle-stop failed, {}", e.getMessage(), e);
            }
            running = false;
        }

        /**
         * @return 本生命周期是否标记为运行中
         */
        @Override
        public boolean isRunning() {
            return running;
        }
    }

}
