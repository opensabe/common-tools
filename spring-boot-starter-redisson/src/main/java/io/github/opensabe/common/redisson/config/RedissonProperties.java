package io.github.opensabe.common.redisson.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "spring.redis.redisson")
public class RedissonProperties {

    private boolean enableSchedule = true;
    private String config;

    private String file;

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public boolean isEnableSchedule() {
        return enableSchedule;
    }

    public void setEnableSchedule(boolean enableSchedule) {
        this.enableSchedule = enableSchedule;
    }
}
