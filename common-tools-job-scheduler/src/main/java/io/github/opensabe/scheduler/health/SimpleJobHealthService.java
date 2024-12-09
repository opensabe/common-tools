package io.github.opensabe.scheduler.health;

import io.github.opensabe.common.utils.AlarmUtil;
import io.github.opensabe.scheduler.conf.SchedulerProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class SimpleJobHealthService {
    private final StringRedisTemplate stringRedisTemplate;
    private final SchedulerProperties schedulerProperties;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private static final long HEALTH_TIMEOUT_IN_MINUTE = 1;

    public SimpleJobHealthService(StringRedisTemplate stringRedisTemplate, SchedulerProperties schedulerProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.schedulerProperties = schedulerProperties;
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(
                this::checkHealth, HEALTH_TIMEOUT_IN_MINUTE * 3, HEALTH_TIMEOUT_IN_MINUTE * 3, TimeUnit.MINUTES
        );
    }

    private String healthKey() {
        return "common-tools-job-scheduler:" + schedulerProperties.getBusinessLine() + ":health";
    }

    public void setHealth() {
        stringRedisTemplate.opsForValue().set(healthKey(), "ok", HEALTH_TIMEOUT_IN_MINUTE, TimeUnit.MINUTES);
        log.info("SimpleJobHealthService: job scheduler set health key");
    }

    public void checkHealth() {
        Boolean aBoolean = stringRedisTemplate.hasKey(healthKey());
        if (aBoolean == null || !aBoolean) {
            AlarmUtil.fatal("SimpleJobHealthService: job scheduler health check failed");
        } else {
            log.info("SimpleJobHealthService: job scheduler health check success");
        }
    }
}
