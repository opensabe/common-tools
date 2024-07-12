package io.github.opensabe.spring.boot.starter.rocketmq;

public final class DefaultMQSendConfig extends MQSendConfig {
    public DefaultMQSendConfig() {
        super(Boolean.TRUE, Boolean.FALSE);
    }
}
