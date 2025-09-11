package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import java.util.concurrent.CountDownLatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MessageRecordPojoWrapper {
    private final MessageRecordPojo messageRecordPojo;
    private final CountDownLatch countDownLatch;
}
