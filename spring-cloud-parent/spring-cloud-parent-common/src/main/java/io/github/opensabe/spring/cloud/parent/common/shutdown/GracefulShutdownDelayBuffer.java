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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;

import io.github.opensabe.spring.cloud.parent.common.config.OnlyOnceApplicationListener;
import lombok.extern.log4j.Log4j2;

/**
 * 优雅关闭延迟缓冲监听器。
 * <p>
 * 目的：
 * <ol>
 *   <li>减少 Web 服务器已关闭但仍有本地连接到达时 XNIO 线程终止导致的
 *       {@code RejectedExecutionException} 报错</li>
 *   <li>减少其他微服务在实例下线前仍调用本实例而收到 503 并触发重试</li>
 * </ol>
 * 实现思路：
 * <ol>
 *   <li>实例须配置 {@code server.shutdown=graceful} 并暴露 {@code /actuator/shutdown}</li>
 *   <li>K8s 通过 {@code /actuator/shutdown} 触发关闭（底层调用 {@code ConfigurableApplicationContext.close()}）</li>
 *   <li>{@code close()} 顺序：发布 {@link ContextClosedEvent}（Eureka 标记 Down）→
 *       {@code lifecycleProcessor.onClose()}（Web 服务器优雅排水）→ destroyBeans → closeBeanFactory → onClose</li>
 *   <li>本监听器在 {@link ContextClosedEvent} 中、于 Eureka 下线之后 sleep
 *       「注册表拉取间隔 + 租约续期间隔 + 缓存预留」秒，等待客户端缓存刷新</li>
 * </ol>
 */
@Log4j2
@ConditionalOnBean({EurekaAutoServiceRegistration.class, EurekaClientConfigBean.class})
@ConditionalOnAvailableEndpoint(endpoint = ShutdownEndpoint.class)
public class GracefulShutdownDelayBuffer extends OnlyOnceApplicationListener<ContextClosedEvent> implements Ordered {

    /**
     * 额外 sleep 秒数，用于抵消 Eureka read cache、客户端 LoadBalancer 缓存等延迟。
     */
    private static final int SLEEP_SECONDS_FOR_CACHE = 5;

    /** @see EurekaClientConfigBean */
    @Autowired(required = false)
    private EurekaClientConfigBean eurekaClientConfigBean;

    /** @see EurekaInstanceConfigBean */
    @Autowired(required = false)
    private EurekaInstanceConfigBean eurekaInstanceConfigBean;

    /** 服务器关闭模式等 Web 服务器属性。 */
    @Autowired
    private ServerProperties serverProperties;

    /**
     * 在优雅关闭且 Eureka 配置可用时，按推算的缓存刷新时间 sleep。
     *
     * @param event 上下文关闭事件
     */
    @Override
    protected void onlyOnce(ContextClosedEvent event) {
        if (Objects.isNull(eurekaClientConfigBean) || Objects.isNull(eurekaInstanceConfigBean)) {
            return;
        }
        if (serverProperties.getShutdown() != null && serverProperties.getShutdown() == Shutdown.GRACEFUL) {
            int registryFetchIntervalSeconds = eurekaClientConfigBean.getRegistryFetchIntervalSeconds();
            int leaseRenewalIntervalInSeconds = eurekaInstanceConfigBean.getLeaseRenewalIntervalInSeconds();
            int sleepSeconds = registryFetchIntervalSeconds + leaseRenewalIntervalInSeconds + SLEEP_SECONDS_FOR_CACHE;
            log.info("GracefulShutdownDelayBuffer-onApplicationEvent start, sleepSeconds: {}", sleepSeconds);
            try {
                TimeUnit.SECONDS.sleep(sleepSeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("GracefulShutdownDelayBuffer-onApplicationEvent complete");
        }
    }

    /**
     * 与 {@link EurekaAutoServiceRegistration} 相同顺序，保证在 Eureka 下线之后执行。
     *
     * @return 监听器顺序值
     */
    @Override
    public int getOrder() {
        return new EurekaAutoServiceRegistration(null, null, null).getOrder();
    }
}
