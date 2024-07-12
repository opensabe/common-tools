package io.github.opensabe.common.alive.client.config;

import lombok.Data;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alive.push")
@Data
public class AliveProperties {

    public static final String ROCKET_CLIENT_NAME = "rocketAliveClient";

    private int product;

    private RocketMQProperties rocketmq;
}
