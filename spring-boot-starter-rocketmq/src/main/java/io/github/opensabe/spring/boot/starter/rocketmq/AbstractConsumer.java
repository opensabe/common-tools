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
import org.springframework.data.util.TypeInformation;

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

@Log4j2
public abstract class AbstractConsumer<T> implements RocketMQListener<MessageExt>, ApplicationListener<ApplicationReadyEvent>, InitializingBean, ConsumerAdjust {
    //用来阻断消费，防止微服务 ApplicationContext 还没启动完全就开始消费
    private final CountDownLatch cdl;
    private final MessageTypeReference<T> typeReference;
    @Autowired
    protected Environment environment;
    protected String topic;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    //用来防止每次消费都要读取 cdl 导致性能下降
    private volatile boolean isStarted = false;

    @SuppressWarnings("unchecked")
    protected AbstractConsumer() {
        TypeInformation<?> information = TypeInformation.of(getClass()).getSuperTypeInformation(AbstractConsumer.class).getTypeArguments().getFirst();
        this.typeReference = (MessageTypeReference<T>) MessageTypeReference.fromTypeInformation(information);
        this.cdl = new CountDownLatch(1);
    }


    @Override
    public void afterPropertiesSet() {
        RocketMQMessageListener rocketMQMessageListener = getClass().getAnnotation(RocketMQMessageListener.class);
        this.topic = environment.resolvePlaceholders(rocketMQMessageListener.topic());
    }


    @SuppressWarnings("unchecked")
    protected BaseMessage<T> convert(MessageExt ext) {
        String payload = new String(ext.getBody(), Charset.defaultCharset());
        // 先进行解码
        String decode = StringUtils.trim(MQMessageUtil.decode(payload));
        if ("v2".equals(ext.getProperty("CORE_VERSION"))) {
            BaseMessage<T> message = null;
            //如果是 json 对象，尝试解析为 BaseMessage
            if (StringUtils.startsWith(decode, "{")) {
                message = JsonUtil.parseObject(decode, typeReference.baseMessageType());
            }
            //如果不以 { 开头，可能是数组。如果解析出来的 message 为空，或者 message.data 为空。
            //这些都尝试直接解析为 T，填入 message.data
            if (Objects.isNull(message)) {
                message = new BaseMessage<>();
            }
            if (Objects.isNull(message.getData())) {
                message.setData(JsonUtil.parseObject(decode, typeReference));
            }
            return message;
        }
        BaseMQMessage v1 = JsonUtil.parseObject(decode, BaseMQMessage.class);
        BaseMessage<T> message = new BaseMessage<>(JsonUtil.parseObject(MQMessageUtil.decode(v1.getData()), typeReference));
        message.setTs(v1.getTs());
        message.setSrc(v1.getSrc());
        message.setTraceId(v1.getTraceId());
        message.setSpanId(v1.getSpanId());
        message.setAction(v1.getAction());
        return message;
    }

    @Override
    public void onMessage(MessageExt ext) {
        if (!isStarted) {
            try {
                long start = System.currentTimeMillis();
                log.info("AbstractMQConsumer-onMessage await for ApplicationReadyEvent...");
                // If the application is not started - wait to consume the messages
                cdl.await();
                log.info("AbstractMQConsumer-onMessage await complete in {}ms", (System.currentTimeMillis() - start));
            } catch (Throwable e) {
                log.error("MQ consume countDownLatch interrupted!", e);
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
                log.info("AbstractMQConsumer-onMessage: topic: {} -> message: {}", topic, trimBodyForLog(new String(ext.getBody())));
            } else {
                log.info("AbstractMQConsumer-onMessage: topic: initial trace id {}, topic: {} -> message: {}", message.getTraceId(), topic, trimBodyForLog(new String(ext.getBody())));
            }
            try {
                onBaseMessage(message);
                messageConsumeContext.setSuccessful(true);
            } catch (Throwable e) {
                log.error("MQ consume failed! {}, {}", new String(ext.getBody()), e.getMessage(), e);
                messageConsumeContext.setSuccessful(false);
                messageConsumeContext.setThrowable(e);
                throw e;
            }
        });
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
        cdl.countDown();
        isStarted = true;
    }


    @Override
    public ConsumeFromWhere consumeFromWhere() {
        return ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
    }

    @Override
    public long consumeFromSecondsAgo() {
        return 10;
    }

    protected abstract void onBaseMessage(BaseMessage<T> baseMessage);
}
