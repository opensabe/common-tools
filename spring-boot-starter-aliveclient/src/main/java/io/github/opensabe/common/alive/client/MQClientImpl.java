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
package io.github.opensabe.common.alive.client;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import io.github.opensabe.common.alive.client.message.MessageVo;
import io.github.opensabe.common.alive.client.message.Publish;
import io.github.opensabe.common.alive.client.message.Response;
import io.github.opensabe.common.alive.client.message.enumeration.MQTopic;
import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import io.github.opensabe.common.alive.client.message.enumeration.RetCode;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageProduceContext;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageProduceObservationConvention;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.RocketMQObservationDocumentation;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;

@Component
public class MQClientImpl implements Client {
    private static final Logger log = LogManager.getLogger(MQClientImpl.class);
    private RocketMQTemplate producer;
    private Integer productCode;
    private UnifiedObservationFactory unifiedObservationFactory;
    private AtomicInteger requestId = new AtomicInteger(1);

    public MQClientImpl(RocketMQTemplate producer, Integer productCode, UnifiedObservationFactory unifiedObservationFactory) {
        this.producer = producer;
        this.productCode = productCode;
        this.unifiedObservationFactory = unifiedObservationFactory;
    }

    public int pushAsync(MessageVo messageVo, ClientCallback callback) {
        final Publish message = this.build(messageVo);
        BaseMQMessage baseMQMessage = wrapMessage(message);
        String topic = this.getTopic(messageVo);
        MessageProduceContext messageProduceContext = new MessageProduceContext(topic);
        messageProduceContext.setMsgLength(baseMQMessage.getData().length());
        Observation observation = RocketMQObservationDocumentation.PRODUCE.observation(
                null, MessageProduceObservationConvention.DEFAULT,
                () -> messageProduceContext, unifiedObservationFactory.getObservationRegistry()
        ).start();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        String traceId = traceContext.traceId();
        String spanId = traceContext.spanId();
        baseMQMessage.setTraceId(traceId);
        baseMQMessage.setSpanId(spanId);

        this.producer.asyncSend(topic, baseMQMessage, new SendCallback() {
            public void onSuccess(SendResult sendResult) {
                messageProduceContext.setSendResult(sendResult.getSendStatus().name());
                observation.stop();
                callback.opComplete(Set.of(Response.builder().retCode(RetCode.SUCCESS).requestId(message.getRequestId()).build()));
            }

            public void onException(Throwable throwable) {
                MQClientImpl.log.error(throwable);
                messageProduceContext.setSendResult("Throwable");
                messageProduceContext.setThrowable(throwable);
                observation.stop();
                callback.opComplete(Set.of(Response.builder().retCode(RetCode.FAIL).requestId(message.getRequestId()).build()));
            }
        });
        return 0;
    }

    private Publish build(MessageVo messageVo) {
        return messageVo.buildPublish(messageVo.getRequestId() == 0 ? this.requestId.incrementAndGet() : messageVo.getRequestId(), this.productCode);
    }

    private String getTopic(MessageVo message) {
        return PushType.GROUP.equals(message.pushType) ? MQTopic.BROAD_CAST.getTopic() : MQTopic.SIMPLE.getTopic();
    }

    private BaseMQMessage wrapMessage(Publish message) {
        final BaseMQMessage baseMQMessage = new BaseMQMessage();
        baseMQMessage.setTs(System.currentTimeMillis());
        baseMQMessage.setData(JsonUtil.toJSONString(message));
        baseMQMessage.setAction("default");
        return baseMQMessage;
    }
}