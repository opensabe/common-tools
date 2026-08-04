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
package io.github.opensabe.spring.cloud.parent.common.config;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.jmx.RingBufferAdminMBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.ConditionalOnEnabledMetricsExport;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.log4j.Log4j2;

/**
 * Log4j2 异步 Logger Ring Buffer 指标配置。
 * <p>
 * 在启用 Prometheus 指标导出且 {@link PrometheusMeterRegistry} 可用时，
 * 于 {@link ContextRefreshedEvent} 后为每个 Logger 注册 Ring Buffer 剩余容量 Gauge。
 * 仅初始化一次，以应对多次 refresh 或多 ApplicationContext 场景。
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
@ConditionalOnEnabledMetricsExport("prometheus")
public class Log4j2Configuration {

    /**
     * Ring Buffer 剩余容量 Gauge 名称后缀。
     */
    public static final String GAUGE_NAME_SUFFIX = "_logger_ring_buffer_remaining_capacity";

    /**
     * Prometheus 指标注册表；可能尚未就绪时通过 {@link ObjectProvider} 延迟获取。
     */
    @Autowired
    private ObjectProvider<PrometheusMeterRegistry> meterRegistry;

    /**
     * 是否已完成 Gauge 注册，保证全局只初始化一次。
     */
    private volatile boolean isInitialized = false;

    /**
     * 在 ApplicationContext 刷新后为每个 Logger 注册 Ring Buffer 剩余容量 Gauge。
     * <p>
     * Log4j2 在 Context 刷新前已初始化；Prometheus Bean 加载时序复杂，
     * 因此在首次 {@link ContextRefreshedEvent} 时注册。Root Logger 在指标名中显示为 {@code root}。
     */
    @EventListener(ContextRefreshedEvent.class)
    public synchronized void init() {
        if (!isInitialized) {
            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            org.apache.logging.log4j.core.config.Configuration configuration = loggerContext.getConfiguration();
            String ctxName = loggerContext.getName();
            configuration.getLoggers().keySet().forEach(k -> {
                try {
                    String cfgName = StringUtils.isBlank(k) ? "" : k;
                    String gaugeName = StringUtils.isBlank(k) ? "root" : k;
                    Gauge.builder(gaugeName + GAUGE_NAME_SUFFIX, () -> {
                        try {
                            return (Number) ManagementFactory.getPlatformMBeanServer()
                                    .getAttribute(new ObjectName(
                                            String.format(RingBufferAdminMBean.PATTERN_ASYNC_LOGGER_CONFIG, ctxName, cfgName)
                                    ), "RemainingCapacity");
                        } catch (InstanceNotFoundException e) {
                            log.warn("{} ring buffer remaining not found", k);
                        } catch (Exception e) {
                            log.error("get {} ring buffer remaining size error", k, e);
                        }
                        return -1;
                    }).register(meterRegistry.getIfAvailable());
                } catch (Exception e) {
                    log.error("Log4j2Configuration-init error: {}", e.getMessage(), e);
                }
            });
            isInitialized = true;
        }
    }
}
