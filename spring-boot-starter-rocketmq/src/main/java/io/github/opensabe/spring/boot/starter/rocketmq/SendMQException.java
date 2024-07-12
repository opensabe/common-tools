package io.github.opensabe.spring.boot.starter.rocketmq;

import org.apache.rocketmq.client.producer.SendResult;

public class SendMQException extends Exception {

    private static final long serialVersionUID = 6258285084638536983L;

    public SendMQException(SendResult sendResult) {
        super(sendResult.toString());
    }
}
