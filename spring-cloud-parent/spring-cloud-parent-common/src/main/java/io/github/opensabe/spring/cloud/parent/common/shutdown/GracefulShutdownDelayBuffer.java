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
package io.github.opensabe.spring.cloud.parent.common.shutdown;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;

import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;

/**
 * 优雅关闭延迟缓冲，目的是：
 * 1. 减少 org.xnio.nio.QueuedNioTcpServer2$$Lambda$2208/0x0000000801dcfbd0@46d8ae74 failed with an exception
 * java.util.concurrent.RejectedExecutionException: XNIO007007: Thread is terminating
 * 这种报错，由于其他微服务可能本地还在请求这个实例，但是这个实例的 WebServer 已经关闭了导致 IO 线程接受了连接但是没有 servlet 线程处理，因为 servlet 线程池已经关闭了
 * 2. 减少其他微服务调用这个实例的时候报 503 导致重试
 * 实现思路是：
 * 1. 实例配置，必须配置优雅关闭 `server.shutdown=graceful`，暴露 `/actuator/shutdown` 接口
 * 2. k8s 通过调用 `/actuator/shutdown` 关闭实例
 * 3. `/actuator/shutdown` 底层是通过调用 ConfigurableApplicationContext.close() 实现的关闭
 * 4. ConfigurableApplicationContext.close() 分为如下几步：
 * 1. publishEvent(new ContextClosedEvent(this)); EurekaAutoConfiguration 会在这一步将实例设置为 Down
 * 2. lifecycleProcessor.onClose();（SmartLifeCycle，Undertow 的 GracefulShutDown 在这一步优雅关闭实例，所有新请求回复 503）
 * 3. destroyBeans(); （调用 Disposable Bean 的 destroy）
 * 4. closeBeanFactory();
 * 5. onClose();
 * 5. 我们通过监听 ContextClosedEvent，同时顺序在 EurekaAutoConfiguration 之后，sleep 当前实例过期时间 + 各种缓存预留时间
 */
@Log4j2
@ConditionalOnBean({EurekaAutoServiceRegistration.class, EurekaClientConfigBean.class})
@ConditionalOnAvailableEndpoint(endpoint = ShutdownEndpoint.class)
public class GracefulShutdownDelayBuffer extends OnlyOnceApplicationListener<ContextClosedEvent> implements Ordered {
    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    /**
     * 用于抵消 eureka read cache 刷新，客户端 loadbalancer 缓存等缓存时间
     */
    private static final int SLEEP_SECONDS_FOR_CACHE = 5;

    @Autowired
    private ObjectProvider<EurekaClientConfigBean> eurekaClientConfigBean;
    @Autowired
    private ObjectProvider<EurekaInstanceConfigBean> eurekaInstanceConfigBean;
    @Autowired
    private ServerProperties serverProperties;

    @Override
    protected void onlyOnce(ContextClosedEvent event) {
        if (serverProperties.getShutdown() != null && serverProperties.getShutdown() == Shutdown.GRACEFUL) {
            //以下均为推测时间，根据本实例的配置，推测其他微服务也是这么配置的
            int registryFetchIntervalSeconds = eurekaClientConfigBean.getIfAvailable()
                    .getRegistryFetchIntervalSeconds();
            int leaseRenewalIntervalInSeconds = eurekaInstanceConfigBean.getIfAvailable()
                    .getLeaseRenewalIntervalInSeconds();
            int sleepSeconds = registryFetchIntervalSeconds + leaseRenewalIntervalInSeconds + SLEEP_SECONDS_FOR_CACHE;
            log.info("GracefulShutdownDelayBuffer-onApplicationEvent start, sleepSeconds: {}", sleepSeconds);
            try {
                TimeUnit.SECONDS.sleep(sleepSeconds);
            } catch (InterruptedException e) {
                //ignore
            }
            log.info("GracefulShutdownDelayBuffer-onApplicationEvent complete");
        }
    }

    @Override
    public int getOrder() {
        //必须在 EurekaAutoServiceRegistration 之后
        return new EurekaAutoServiceRegistration(null, null, null).getOrder();
    }
}
