package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import com.github.javafaker.Faker;

public record MessageRecordPojo(
    String text, Integer number, Long timestamp, Double cost
) {


    public static MessageRecordPojo getSecretMessageRecordPojo() {
        return new MessageRecordPojo(
                "this is a secret message",
                new Faker().number().randomDigit(),
                System.currentTimeMillis(),
                new Faker().number().randomDouble(10, 1, 1000)
        );
    }
}
