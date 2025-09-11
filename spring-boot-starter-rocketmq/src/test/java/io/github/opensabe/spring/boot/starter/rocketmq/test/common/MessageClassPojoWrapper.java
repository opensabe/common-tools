package io.github.opensabe.spring.boot.starter.rocketmq.test.common;

import java.util.concurrent.CountDownLatch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class MessageClassPojoWrapper {
    private final MessageClassPojo messageClassPojo;
    private final CountDownLatch countDownLatch;
}
