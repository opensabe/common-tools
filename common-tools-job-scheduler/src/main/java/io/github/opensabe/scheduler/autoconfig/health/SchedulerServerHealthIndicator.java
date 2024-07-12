package io.github.opensabe.scheduler.autoconfig.health;

import io.github.opensabe.scheduler.conf.SchedulerProperties;
import io.github.opensabe.scheduler.server.SchedulerServer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

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
                    health.up().withDetail("schedulerServer", "enabled") .withDetail("schedulerServer", "running");
                } else {
                    health.down().withDetail("schedulerServer", "enabled").withDetail("schedulerServer", "stopped");
                }
            });
        }
        return health.build();
    }
}
