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
package io.github.opensabe.scheduler.autoconfig.health;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import io.github.opensabe.scheduler.conf.SchedulerProperties;
import io.github.opensabe.scheduler.server.SchedulerServer;

/**
 * 调度服务端 Actuator 健康指示器。
 * <p>
 * 根据 {@link SchedulerProperties#isEnable()} 与 {@link SchedulerServer#isRunning()} 上报 UP/DOWN/OUT_OF_SERVICE。
 */
public class SchedulerServerHealthIndicator implements HealthIndicator {

    /**
     * 调度服务端 Bean 提供者，未启用调度时可能不存在。
     */
    private final ObjectProvider<SchedulerServer> schedulerServerProvider;

    /**
     * 调度任务配置属性。
     */
    private final SchedulerProperties schedulerProperties;

    /**
     * @param schedulerProperties     调度配置
     * @param schedulerServerProvider 调度服务端可选提供者
     */
    public SchedulerServerHealthIndicator(SchedulerProperties schedulerProperties, ObjectProvider<SchedulerServer> schedulerServerProvider) {
        this.schedulerServerProvider = schedulerServerProvider;
        this.schedulerProperties = schedulerProperties;
    }

    /**
     * 构建调度服务端健康状态。
     *
     * @return 健康检查结果
     */
    @Override
    public Health health() {
        final Health.Builder health = Health.unknown();
        if (!schedulerProperties.isEnable()) {
            health.outOfService().withDetail("schedulerServer", "disabled");
        } else {
            schedulerServerProvider.ifAvailable(schedulerServer -> {
                if (schedulerServer.isRunning()) {
                    health.up().withDetail("enabled", true).withDetail("status", "running");
                } else {
                    health.down().withDetail("enabled", true).withDetail("status", "stopped");
                }
            });
        }
        return health.build();
    }
}
