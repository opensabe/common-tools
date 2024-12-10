package io.github.opensabe.scheduler.health;

import io.github.opensabe.scheduler.conf.SimpleTask;
import io.github.opensabe.scheduler.job.SimpleJob;

@SimpleTask(cron = "0/10 * * * * ? ")
public class HealthCheckJob implements SimpleJob {
    private final SimpleJobHealthService simpleJobHealthService;

    public HealthCheckJob(SimpleJobHealthService simpleJobHealthService) {
        this.simpleJobHealthService = simpleJobHealthService;
    }

    @Override
    public void execute() {
        simpleJobHealthService.setHealth();
    }
}
