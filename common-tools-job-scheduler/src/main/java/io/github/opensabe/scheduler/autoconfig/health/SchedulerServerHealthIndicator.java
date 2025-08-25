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
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import io.github.opensabe.scheduler.conf.SchedulerProperties;
import io.github.opensabe.scheduler.server.SchedulerServer;

public class SchedulerServerHealthIndicator implements HealthIndicator {

    private final ObjectProvider<SchedulerServer> schedulerServerProvider;
    private final SchedulerProperties schedulerProperties;

    public SchedulerServerHealthIndicator(SchedulerProperties schedulerProperties, ObjectProvider<SchedulerServer> schedulerServerProvider) {
        this.schedulerServerProvider = schedulerServerProvider;
        this.schedulerProperties = schedulerProperties;
    }

    @Override
    public Health health() {
        final Health.Builder health = Health.unknown();
        if (!schedulerProperties.isEnable()) {
            health.outOfService().withDetail("schedulerServer", "disabled");
        } else {
            schedulerServerProvider.ifAvailable(schedulerServer -> {
                if (schedulerServer.isRunning()) {
                    health.up().withDetail("schedulerServer", "enabled").withDetail("schedulerServer", "running");
                } else {
                    health.down().withDetail("schedulerServer", "enabled").withDetail("schedulerServer", "stopped");
                }
            });
        }
        return health.build();
    }
}
