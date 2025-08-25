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
package io.github.opensabe.spring.cloud.parent.web.common.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.export.ConditionalOnEnabledMetricsExport;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

@Log4j2
@Configuration(proxyBeanMethods = false)
//需要在引入了 prometheus 并且 actuator 暴露了 prometheus 端口的情况下才加载
@ConditionalOnEnabledMetricsExport("prometheus")
public class UndertowXNIOConfiguration {
    @Autowired
    private ObjectProvider<PrometheusMeterRegistry> meterRegistry;
    //只初始化一次
    private volatile boolean isInitialized = false;

    //需要在 ApplicationContext 刷新之后进行注册
    //在加载 ApplicationContext 之前，日志配置就已经初始化好了
    //但是 prometheus 的相关 Bean 加载比较复杂，并且随着版本更迭改动比较多，所以就直接偷懒，在整个 ApplicationContext 刷新之后再注册
    // ApplicationContext 可能 refresh 多次，例如调用 /actuator/refresh，还有就是多 ApplicationContext 的场景
    // 这里为了简单，通过一个简单的 isInitialized 判断是否是第一次初始化，保证只初始化一次
    @EventListener(ContextRefreshedEvent.class)
    public synchronized void init() {
        if (!isInitialized) {
            Gauge.builder("http_servlet_queue_size", () ->
            {
                try {
                    return (Integer) ManagementFactory.getPlatformMBeanServer()
                            .getAttribute(new ObjectName("org.xnio:type=Xnio,provider=\"nio\",worker=\"XNIO-2\""), "WorkerQueueSize");
                } catch (Exception e) {
                    log.error("get http_servlet_queue_size error", e);
                }
                return -1;
            }).register(meterRegistry.getIfAvailable());
            isInitialized = true;
        }
    }
}
