/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.spring.boot.starter.rocketmq.test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson.JSON;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.entity.base.vo.BaseMessage;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractConsumer;
import io.github.opensabe.spring.boot.starter.rocketmq.AbstractMQConsumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * MQ V1/V2 信封与内层 JsonUtil/Fastjson 交叉兼容契约测试。
 */
@DisplayName("MQ V1↔V2 交叉兼容（信封 JsonUtil；内层 JsonUtil/Fastjson）")
class MQCrossVersionWireContractTest {

    /** sample。 */
    private static CompatPayload sample() {
        LocalDateTime updated = LocalDateTime.of(2024, 6, 15, 12, 30, 45).truncatedTo(ChronoUnit.MILLIS);
        return new CompatPayload(
                "id-001",
                2,
                updated,
                Date.from(updated.atZone(ZoneId.systemDefault()).toInstant())
        );
    }

    /**
     * V1 双编码信封经 {@link AbstractConsumer} 消费，内层 JsonUtil 解析。
     */
    @Test
    @DisplayName("新产老消：V2 object data → AbstractMQConsumer；内层 JsonUtil 与 Fastjson")
    void oldProduceNewConsumeV1() {
        CompatPayload payload = sample();
        BaseMQMessage envelope = new BaseMQMessage();
        envelope.setTraceId("trace-1");
        envelope.setSpanId("span-1");
        envelope.setTs(System.currentTimeMillis());
        envelope.setSrc("core-service");
        envelope.setAction("default");
        envelope.setData(JsonUtil.toJSONString(payload));

        MessageExt ext = new MessageExt();
        ext.setBody(JsonUtil.toJSONString(envelope).getBytes(StandardCharsets.UTF_8));

        CompatPayloadConsumer consumer = new CompatPayloadConsumer();
        consumer.onMessage(ext);
        assertPayload(payload, consumer.last);
    }

    /**
     * V1 信封内层 Fastjson 编码，{@link AbstractConsumer} 仍可用 JsonUtil 解析。
     */
    @Test
    void oldProduceNewConsumeV1FastjsonInner() {
        CompatPayload payload = sample();
        BaseMQMessage envelope = new BaseMQMessage();
        envelope.setTraceId("trace-1b");
        envelope.setSpanId("span-1b");
        envelope.setTs(System.currentTimeMillis());
        envelope.setSrc("core-service");
        envelope.setAction("default");
        envelope.setData(JSON.toJSONString(payload));

        MessageExt ext = new MessageExt();
        ext.setBody(JsonUtil.toJSONString(envelope).getBytes(StandardCharsets.UTF_8));

        CompatPayloadConsumer consumer = new CompatPayloadConsumer();
        consumer.onMessage(ext);
        assertPayload(payload, consumer.last);
    }

    /**
     * V2 object data 信封经 {@link AbstractMQConsumer} 消费，内层 JsonUtil 与 Fastjson 均可读。
     */
    @Test
    void newProduceOldConsumeV2() {
        CompatPayload payload = sample();
        BaseMessage<CompatPayload> envelope = new BaseMessage<>();
        envelope.setTraceId("trace-2");
        envelope.setSpanId("span-2");
        envelope.setTs(System.currentTimeMillis());
        envelope.setSrc("core-service");
        envelope.setAction("default");
        envelope.setData(payload);

        MessageExt ext = new MessageExt();
        ext.putUserProperty("CORE_VERSION", "v2");
        ext.setBody(JsonUtil.toJSONString(envelope).getBytes(StandardCharsets.UTF_8));

        LegacyMqConsumer consumer = new LegacyMqConsumer();
        consumer.onMessage(ext);
        assertPayload(payload, consumer.viaJsonUtil);
        assertPayload(payload, consumer.viaFastjson);
    }

    /** assertPayload。 */
    private static void assertPayload(CompatPayload expected, CompatPayload actual) {
        assertNotNull(actual);
        assertEquals(expected.getEntityId(), actual.getEntityId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getUpdatedAt(), actual.getUpdatedAt());
        assertEquals(expected.getCreatedAt().getTime(), actual.getCreatedAt().getTime());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompatPayload {
/** entityId。 */
        private String entityId;
/** status。 */
        private int status;
/** updatedAt。 */
        private LocalDateTime updatedAt;
/** createdAt。 */
        private Date createdAt;
    }

    static class CompatPayloadConsumer extends AbstractConsumer<CompatPayload> {
        CompatPayload last;

        /** {@inheritDoc} */
        @Override
        public void onMessage(MessageExt ext) {
            onBaseMessage(convert(ext));
        }

        /** {@inheritDoc} */
        @Override
        protected void onBaseMessage(BaseMessage<CompatPayload> baseMessage) {
            last = baseMessage.getData();
        }
    }

    static class LegacyMqConsumer extends AbstractMQConsumer {
        CompatPayload viaJsonUtil;
        CompatPayload viaFastjson;

        /** {@inheritDoc} */
        @Override
        public void onMessage(MessageExt ext) {
            onBaseMessage(convert(ext));
        }

        /** {@inheritDoc} */
        @Override
        protected void onBaseMQMessage(BaseMQMessage baseMQMessage) {
            viaJsonUtil = JsonUtil.parseObject(baseMQMessage.getData(), CompatPayload.class);
            viaFastjson = JSON.parseObject(baseMQMessage.getData(), CompatPayload.class);
        }
    }
}
