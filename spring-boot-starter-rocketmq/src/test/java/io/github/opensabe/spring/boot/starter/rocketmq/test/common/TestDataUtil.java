package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import com.github.javafaker.Faker;

public class TestDataUtil {

    private static final Faker FAKER = new Faker(Locale.CHINA);

    public static final String SECRET_MESSAGE = "SecretMessage";

    public static final String TEST_RECORD_TOPIC = "rocketmq-test-record-topic";
    public static final String TEST_CLASS_TOPIC = "rocketmq-test-class-topic";

    public static MessageRecordPojoWrapper getNormalMessageRecordPojo() {
        return MessageRecordPojoWrapper.builder().messageRecordPojo(
                new MessageRecordPojo(
                        FAKER.address().fullAddress(),
                        FAKER.number().randomDigit(),
                        System.currentTimeMillis(),
                        FAKER.number().randomDouble(10, 1, 1000)
                )
        ).countDownLatch(new CountDownLatch(1)).build();
    }

    public static MessageClassPojoWrapper getNormalMessageClassPojo() {
        return MessageClassPojoWrapper.builder().messageClassPojo(
                new MessageClassPojo(
                        FAKER.address().fullAddress(),
                        FAKER.number().randomDigit(),
                        System.currentTimeMillis(),
                        FAKER.number().randomDouble(10, 1, 1000)
                )
        ).countDownLatch(new CountDownLatch(1)).build();
    }

    public static MessageRecordPojoWrapper getSecretMessageRecordPojo() {
        return MessageRecordPojoWrapper.builder().messageRecordPojo(
                new MessageRecordPojo(
                        SECRET_MESSAGE,
                        FAKER.number().randomDigit(),
                        System.currentTimeMillis(),
                        FAKER.number().randomDouble(10, 1, 1000)
                )
        ).countDownLatch(new CountDownLatch(1)).build();
    }

    public static MessageClassPojoWrapper getSecretMessageClassPojo() {
        return MessageClassPojoWrapper.builder().messageClassPojo(
                new MessageClassPojo(
                        SECRET_MESSAGE,
                        FAKER.number().randomDigit(),
                        System.currentTimeMillis(),
                        FAKER.number().randomDouble(10, 1, 1000)
                )
        ).countDownLatch(new CountDownLatch(1)).build();
    }

    public static MessageRecordPojoWrapper getLargeMessageRecordPojo() {
        return MessageRecordPojoWrapper.builder().messageRecordPojo(
                new MessageRecordPojo(
                        // 4MB
                        generateLargeMessage(1024 * 1024 * 4),
                        FAKER.number().randomDigit(),
                        System.currentTimeMillis(),
                        FAKER.number().randomDouble(10, 1, 1000)
                )
        ).countDownLatch(new CountDownLatch(1)).build();
    }

    public static MessageClassPojoWrapper getLargeMessageClassPojo() {
        return MessageClassPojoWrapper.builder().messageClassPojo(
                new MessageClassPojo(
                        // 4MB
                        generateLargeMessage(1024 * 1024 * 4),
                        FAKER.number().randomDigit(),
                        System.currentTimeMillis(),
                        FAKER.number().randomDouble(10, 1, 1000)
                )
        ).countDownLatch(new CountDownLatch(1)).build();
    }

    private static String generateLargeMessage(int size) {
        StringBuilder stringB = new StringBuilder(size);
        String paddingString = "abcdefghijklmnopqrs";

        while (stringB.length() + paddingString.length() < size) {
            stringB.append(paddingString);
        }

        return stringB.toString();
    }
}
