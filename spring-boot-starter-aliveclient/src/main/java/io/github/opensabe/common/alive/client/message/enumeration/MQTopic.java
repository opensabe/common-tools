package io.github.opensabe.common.alive.client.message.enumeration;

public enum MQTopic {
    BROAD_CAST("opensabe_common_alive_broadcast"),
    SIMPLE("opensabe_common_alive_simple");

    private String topic;

    public String getTopic() {
        return this.topic;
    }

    private MQTopic(final String topic) {
        this.topic = topic;
    }
}