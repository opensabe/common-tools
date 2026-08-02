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
package io.github.opensabe.spring.boot.starter.rocketmq;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.data.core.TypeInformation;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.entity.base.vo.BaseMessage;
import io.github.opensabe.common.entity.base.vo.MessageTypeReference;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageConsumeContext;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageConsumeObservationConvention;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.RocketMQObservationDocumentation;
import io.micrometer.observation.Observation;
import jakarta.annotation.Nonnull;
import lombok.extern.log4j.Log4j2;

import static io.github.opensabe.spring.boot.starter.rocketmq.MQMessageUtil.trimBodyForLog;

/**
 * RocketMQ 消费者抽象基类（V1/V2 消息线兼容）。
 * <p>
 * 在 {@link ApplicationReadyEvent} 之前阻塞消费，避免 ApplicationContext 未就绪时处理消息；
 * 消费过程包裹 Micrometer observation 并支持 {@link BaseMessage} 泛型解析。
 *
 * @param <T> 消息体类型
 */
@Log4j2
public abstract class AbstractConsumer<T> implements RocketMQListener<MessageExt>, ApplicationListener<ApplicationReadyEvent>, InitializingBean, ConsumerAdjust {

    /** 阻断消费直至应用就绪。 */
    private final CountDownLatch cdl;

    /** 从子类泛型参数推导的 Jackson 类型引用。 */
    private final MessageTypeReference<T> typeReference;

    /** Spring 环境，用于解析 {@link RocketMQMessageListener#topic()} 占位符。 */
    @Autowired
    protected Environment environment;

    /** 解析后的消费主题。 */
    protected String topic;

    /** 统一观测工厂。 */
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;

    /** 应用就绪后为 {@code true}，避免每次消费检查 {@link #cdl}。 */
    private volatile boolean isStarted = false;

    /**
     * 从子类声明的 {@code AbstractConsumer<T>} 泛型参数初始化 {@link #typeReference}。
     */
    @SuppressWarnings("unchecked")
    protected AbstractConsumer() {
        TypeInformation<?> information = TypeInformation.of(getClass()).getSuperTypeInformation(AbstractConsumer.class).getTypeArguments().getFirst();
        this.typeReference = (MessageTypeReference<T>) MessageTypeReference.fromTypeInformation(information);
        this.cdl = new CountDownLatch(1);
    }

    /** 解析 {@link RocketMQMessageListener#topic()} 并写入 {@link #topic}。 */
    @Override
    public void afterPropertiesSet() {
        RocketMQMessageListener rocketMQMessageListener = getClass().getAnnotation(RocketMQMessageListener.class);
        this.topic = environment.resolvePlaceholders(rocketMQMessageListener.topic());
    }

    /**
     * 按 {@code CORE_VERSION} 属性在 V1/V2 格式间分发反序列化。
     *
     * @param ext RocketMQ 原始消息
     * @return 解析后的 {@link BaseMessage}
     */
    @SuppressWarnings("unchecked")
    protected BaseMessage<T> convert(MessageExt ext) {
        String payload = new String(ext.getBody(), Charset.defaultCharset());
        String decode = StringUtils.trim(MQMessageUtil.decode(payload));
        if ("v2".equals(ext.getProperty("CORE_VERSION"))) {
            return convertV2(decode);
        }
        BaseMQMessage v1 = convertV1(decode);
        BaseMessage<T> message = new BaseMessage<>(JsonUtil.parseObject(v1.getData(), typeReference));
        message.setTs(v1.getTs());
        message.setSrc(v1.getSrc());
        message.setTraceId(v1.getTraceId());
        message.setSpanId(v1.getSpanId());
        message.setAction(v1.getAction());
        return message;
    }

    /**
     * V2 线反序列化：优先解析为 {@link BaseMessage}；失败或 {@code data} 为空时直接解析为 {@code T}。
     *
     * @param decode 解码后的消息体
     * @return 消息信封
     */
    protected BaseMessage<T> convertV2(String decode) {
        BaseMessage<T> message = null;
        if (StringUtils.startsWith(decode, "{")) {
            message = JsonUtil.parseObject(decode, typeReference.baseMessageType());
        }
        if (Objects.isNull(message)) {
            message = new BaseMessage<>();
        }
        if (Objects.isNull(message.getData())) {
            message.setData(JsonUtil.parseObject(decode, typeReference));
        }
        return message;
    }

    /**
     * V1 线反序列化：{@link BaseMQMessage} 包装；{@code data} 为空时使用原始消息体（未包装兼容）。
     *
     * @param decode 解码后的消息体
     * @return V1 信封
     */
    protected BaseMQMessage convertV1(String decode) {
        BaseMQMessage baseMQMessage = JsonUtil.parseObject(decode, BaseMQMessage.class);
        if (Objects.isNull(baseMQMessage)) {
            baseMQMessage = new BaseMQMessage();
        }
        if (StringUtils.isBlank(baseMQMessage.getData())) {
            baseMQMessage.setData(decode);
        }
        baseMQMessage = MQMessageUtil.decode(baseMQMessage);
        return baseMQMessage;
    }

    /** {@inheritDoc} — 等待就绪后转换消息并记录消费 observation。 */
    @Override
    public void onMessage(MessageExt ext) {
        if (!isStarted) {
            try {
                long start = System.currentTimeMillis();
                log.info("AbstractConsumer awaiting ApplicationReadyEvent before consuming...");
                cdl.await();
                log.info("AbstractConsumer ApplicationReadyEvent wait completed in {}ms", (System.currentTimeMillis() - start));
            } catch (Throwable e) {
                log.error("MQ consume CountDownLatch interrupted", e);
            }
        }

        BaseMessage<T> message = convert(ext);
        MessageConsumeContext messageConsumeContext = new MessageConsumeContext(message.getTraceId(), topic);
        Observation observation = RocketMQObservationDocumentation.CONSUME.observation(
                null, MessageConsumeObservationConvention.DEFAULT,
                () -> messageConsumeContext, unifiedObservationFactory.getObservationRegistry()
        );

        observation.observe(() -> {
            if (StringUtils.isEmpty(message.getTraceId())) {
                log.info("AbstractConsumer onMessage: topic={} body={}", topic, trimBodyForLog(new String(ext.getBody())));
            } else {
                log.info("AbstractConsumer onMessage: traceId={} topic={} body={}", message.getTraceId(), topic, trimBodyForLog(new String(ext.getBody())));
            }
            try {
                onBaseMessage(message);
                messageConsumeContext.setSuccessful(true);
            } catch (Throwable e) {
                log.error("MQ consume failed: body={} error={}", new String(ext.getBody()), e.getMessage(), e);
                messageConsumeContext.setSuccessful(false);
                messageConsumeContext.setThrowable(e);
                throw e;
            }
        });
    }

    /** 应用就绪后释放 {@link #cdl} 并标记 {@link #isStarted}。 */
    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
        cdl.countDown();
        isStarted = true;
    }

    /** {@inheritDoc} */
    @Override
    public ConsumeFromWhere consumeFromWhere() {
        return ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
    }

    /** {@inheritDoc} */
    @Override
    public long consumeFromSecondsAgo() {
        return 10;
    }

    /**
     * 子类实现的业务消费逻辑。
     *
     * @param baseMessage 已解析的消息信封
     */
    protected abstract void onBaseMessage(BaseMessage<T> baseMessage);
}
