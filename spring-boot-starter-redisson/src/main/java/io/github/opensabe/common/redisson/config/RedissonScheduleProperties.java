package io.github.opensabe.common.redisson.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "spring.redis.redisson.enable")
public class RedissonScheduleProperties {

    private boolean schedule = true;

    public boolean isEnableSchedule() {
        return schedule;
    }

    public void setEnableSchedule(boolean enableSchedule) {
        this.schedule = enableSchedule;
    }
}
