package io.github.opensabe.spring.boot.starter.rocketmq;

public final class DefaultMQSendConfig extends MQSendConfig {
    public DefaultMQSendConfig() {
        /*
         * 20261114 新增支持发送 18个 delay level，但这里将delayLevel设置成null
         */
        super(Boolean.TRUE, Boolean.FALSE, null);
    }
}
