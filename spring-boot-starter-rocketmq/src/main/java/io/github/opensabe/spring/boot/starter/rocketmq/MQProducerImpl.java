package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.idgenerator.service.UniqueID;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.secret.FilterSecretStringResult;
import io.github.opensabe.common.secret.GlobalSecretManager;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageProduceContext;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.MessageProduceObservationConvention;
import io.github.opensabe.spring.boot.starter.rocketmq.observation.RocketMQObservationDocumentation;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Objects;
import java.util.Optional;

@Log4j2
public class MQProducerImpl implements MQProducer {
    private final String srcName;
    private final UnifiedObservationFactory unifiedObservationFactory;
    private final RocketMQTemplate rocketMQTemplate;

    private final MessagePersistent persistent;
    private final UniqueID uniqueID;

    private final GlobalSecretManager globalSecretManager;

    public MQProducerImpl(String srcName, UnifiedObservationFactory unifiedObservationFactory, RocketMQTemplate rocketMQTemplate, MessagePersistent persistent, UniqueID uniqueID, GlobalSecretManager globalSecretManager) {
        this.srcName = srcName;
        this.unifiedObservationFactory = unifiedObservationFactory;
        this.rocketMQTemplate = rocketMQTemplate;
        this.persistent = persistent;
        this.uniqueID = uniqueID;
        this.globalSecretManager = globalSecretManager;
    }


    @Override
    public void send(String topic, Object o) {
        send(topic, o, null, false);
    }

    @Override
    public void send(String topic, Object o, Long time) {
       send(topic,o,null,false,time);
    }

    @Override
    public void send(String topic, Object o, MQSendConfig mqSendConfig) {
        send(topic, o, null, false, mqSendConfig);
    }

    @Override
    public void sendAsync(String topic, Object o) {
        send(topic, o, null, true, null, null);
    }

    @Override
    public void sendAsync(String topic, Object o, Long time) {
        send(topic,o,null,true,time,null,null);
    }

    @Override
    public void sendAsync(String topic, Object o, MQSendConfig mqSendConfig) {
        send(topic, o, null, true, null, mqSendConfig);
    }

    @Override
    public void send(String topic, Object o, boolean isAsync) {
        send(topic, o, null, isAsync);
    }

    @Override
    public void send(String topic, Object o, boolean isAsync, Long time) {
        send(topic, o, null, isAsync,time);
    }

    @Override
    public void send(String topic, Object o, boolean isAsync, MQSendConfig mqSendConfig) {
        send(topic, o, null, isAsync, mqSendConfig);
    }

    @Override
    public void sendAsync(String topic, Object o, SendCallback sendCallback) {
        send(topic, o, null, true, sendCallback, null);
    }

    @Override
    public void sendAsync(String topic, Object o, SendCallback sendCallback, Long time) {
        send(topic, o, null, true,time, sendCallback, null);
    }

    @Override
    public void sendAsync(String topic, Object o, SendCallback sendCallback, MQSendConfig mqSendConfig) {
        send(topic, o, null, true, sendCallback, mqSendConfig);
    }

    @Override
    public void send(String topic, Object o, String hashKey) {
        send(topic, o, hashKey, false);
    }

    @Override
    public void send(String topic, Object o, String hashKey, Long time) {
        send(topic, o, hashKey, false,time);
    }

    @Override
    public void send(String topic, Object o, String hashKey, MQSendConfig mqSendConfig) {
        send(topic, o, hashKey, false, mqSendConfig);
    }

    @Override
    public void sendAsync(String topic, Object o, String hashKey, SendCallback sendCallback) {
        send(topic, o, hashKey, true, sendCallback, null);
    }

    @Override
    public void sendAsync(String topic, Object o, String hashKey, SendCallback sendCallback, Long time) {
        send(topic, o, hashKey, true,time, sendCallback, null);
    }

    @Override
    public void sendAsync(String topic, Object o, String hashKey, SendCallback sendCallback, MQSendConfig mqSendConfig) {
        send(topic, o, hashKey, true, sendCallback, mqSendConfig);
    }

    @Override
    public void send(String topic, Object o, String hashKey, boolean isAsync) {
        send(topic, o, hashKey, isAsync, null, null);
    }

    @Override
    public void send(String topic, Object o, String hashKey, boolean isAsync, Long time) {
        send(topic,o,hashKey,isAsync,time,null,null);
    }

    @Override
    public void send(String topic, Object o, String hashKey, boolean isAsync, MQSendConfig mqSendConfig) {
        send(topic, o, hashKey, isAsync, null, mqSendConfig);
    }

    private void handleSendResult(
            MQSendConfig mqSendConfig, String topic, String hashKey, String traceIdString,
            BaseMQMessage baseMQMessage, MessageProduceContext messageProduceContext, Observation observation,
            SendCallback sendCallback, SendResult sendResult
    ) {
        if (sendResult != null) {
            messageProduceContext.setSendResult(sendResult.getSendStatus().toString());
            if (Objects.equals(sendResult.getSendStatus(), SendStatus.SEND_OK)) {
                log.info("MQProducerImpl-handleSendResult: success, result: {}", sendResult);
                if (sendCallback != null) {
                    try {
                        sendCallback.onSuccess(sendResult);
                    } catch (Throwable e) {
                        log.error("MQProducerImpl-handleSendResult sendCallback onSuccess error: {}", e.getMessage(), e);
                    }
                }
            } else {
                log.fatal("MQProducerImpl-handleSendResult: failed, result: {}", sendResult);
                SendMQException sendMQException = new SendMQException(sendResult);
                messageProduceContext.setThrowable(sendMQException);
                if (sendCallback != null) {
                    try {
                        sendCallback.onException(sendMQException);
                    } catch (Throwable e) {
                        log.error("MQProducerImpl-handleSendResult sendCallback onException error: {}", e.getMessage(), e);
                    }
                }
                failThenPersist(mqSendConfig, topic, hashKey, traceIdString, baseMQMessage);
            }
        }
        observation.stop();
    }

    private void handleSendException(
            MQSendConfig mqSendConfig, String topic, String hashKey, String traceIdString,
            BaseMQMessage baseMQMessage, MessageProduceContext messageProduceContext,
            Observation observation, SendCallback sendCallback,
            Throwable throwable) {
        log.fatal("MQProducerImpl-handleSendException: message = {}, topic = {}", baseMQMessage, topic, throwable);
        failThenPersist(mqSendConfig, topic, hashKey, traceIdString, baseMQMessage);
        if (sendCallback != null) {
            sendCallback.onException(throwable);
        }
        messageProduceContext.setThrowable(throwable);
        observation.stop();
    }

    @Override
    public void send(String topic, Object o, String hashKey, boolean isAsync, SendCallback sendCallback, MQSendConfig mqSendConfig) {
        send(topic,o,hashKey,isAsync,null,sendCallback,mqSendConfig);
    }

    @Override
    public void send(String topic, Object o, String hashKey, boolean isAsync, Long time, SendCallback sendCallback, MQSendConfig mqSendConfig) {
        if (Objects.isNull(mqSendConfig)) {
            mqSendConfig = new DefaultMQSendConfig();
        }
        final MQSendConfig mqSendConfigFinal = mqSendConfig;

        MessageProduceContext messageProduceContext = new MessageProduceContext(topic);
        Observation observation = RocketMQObservationDocumentation.PRODUCE.observation(
                null, MessageProduceObservationConvention.DEFAULT,
                () -> messageProduceContext, unifiedObservationFactory.getObservationRegistry()
        ).start();

        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        String traceId = traceContext.traceId();
        String spanId = traceContext.spanId();
        try {
            SendResult sendResult;
            log.info("Try send to MQ, topic: {}, hashKey: {}, isAsync: {}, data: {}", () -> topic, () -> hashKey, () -> isAsync, o::toString);
            BaseMQMessage baseMQMessage;
            if (o instanceof BaseMQMessage) {
                baseMQMessage = (BaseMQMessage) o;
            } else {
                baseMQMessage = new BaseMQMessage();
                baseMQMessage.setData(JsonUtil.toJSONString(o));
                baseMQMessage.setAction("default");
            }
            FilterSecretStringResult filterSecretStringResult = globalSecretManager.filterSecretStringAndAlarm(baseMQMessage.getData());
            if (filterSecretStringResult.isFoundSensitiveString()) {
                throw new RuntimeException("Sensitive string found in MQ message");
            }

            baseMQMessage.setTraceId(traceId);
            baseMQMessage.setSpanId(spanId);
            baseMQMessage.setSrc(srcName);
            baseMQMessage.setTs(Objects.isNull(time) ? System.currentTimeMillis() : time);

            if (Optional.ofNullable(mqSendConfigFinal.getIsCompressEnabled()).orElse(false)) {
                // compress the message if its size > 4MB
                MQMessageUtil.encode(baseMQMessage);
            }

            messageProduceContext.setMsgLength(StringUtils.isNotBlank(baseMQMessage.getData()) ? baseMQMessage.getData().length() : 0);

            final BaseMQMessage baseMQMessageFinal = baseMQMessage;
            Message<?> message = MessageBuilder.withPayload(baseMQMessage).setHeader("KEYS", traceId).build();

            if (isAsync) {
                SendCallback sendCallbackForAsync = new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        handleSendResult(
                                mqSendConfigFinal, topic, hashKey, traceId, baseMQMessageFinal,
                                messageProduceContext, observation, sendCallback, sendResult
                        );
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        handleSendException(
                                mqSendConfigFinal, topic, hashKey, traceId, baseMQMessageFinal,
                                messageProduceContext, observation, sendCallback, throwable
                        );
                    }
                };
                if (StringUtils.isNotBlank(hashKey)) {
                    rocketMQTemplate.asyncSendOrderly(topic, message, hashKey, sendCallbackForAsync);
                } else {
                    rocketMQTemplate.asyncSend(topic, message, sendCallbackForAsync);
                }
            } else {
                try {
                    if (StringUtils.isNotBlank(hashKey)) {
                        sendResult = rocketMQTemplate.syncSendOrderly(topic, message, hashKey);
                    } else {
                        sendResult = rocketMQTemplate.syncSend(topic, message);
                    }
                    handleSendResult(
                            mqSendConfigFinal, topic, hashKey, traceId, baseMQMessageFinal,
                            messageProduceContext,observation, sendCallback, sendResult
                    );
                } catch (Throwable e) {
                    handleSendException(
                            mqSendConfigFinal, topic, hashKey, traceId, baseMQMessageFinal,
                            messageProduceContext, observation, sendCallback, e
                    );
                }
            }
        } catch (Throwable e) {
            messageProduceContext.setSendResult("Throwable");
            messageProduceContext.setThrowable(e);
            observation.stop();
            throw e;
        }

    }
    @Override
    public void sendWithInTransaction(String topic, Object body, Object transactionObj, UniqueRocketMQLocalTransactionListener uniqueRocketMQLocalTransactionListener) {
        MessageProduceContext messageProduceContext = new MessageProduceContext(topic);
        Observation observation = RocketMQObservationDocumentation.PRODUCE.observation(
                null, MessageProduceObservationConvention.DEFAULT,
                () -> messageProduceContext, unifiedObservationFactory.getObservationRegistry()
        );
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
        String traceId = traceContext.traceId();
        String spanId = traceContext.spanId();
        observation.observe(() -> {
            try {
                log.info("Try send to MQ, topic: {}, data: {}, transactionObj: {}", () -> topic, body::toString, transactionObj::toString);
                BaseMQMessage baseMQMessage;
                if (body instanceof BaseMQMessage) {
                    baseMQMessage = (BaseMQMessage) body;
                } else {
                    baseMQMessage = new BaseMQMessage();
                    baseMQMessage.setData(JsonUtil.toJSONString(body));
                    baseMQMessage.setAction("default");
                }
                baseMQMessage.setTraceId(traceId);
                baseMQMessage.setSpanId(spanId);
                baseMQMessage.setSrc(srcName);
                baseMQMessage.setTs(System.currentTimeMillis());

                messageProduceContext.setMsgLength(StringUtils.isNotBlank(baseMQMessage.getData()) ? baseMQMessage.getData().length() : 0);

                Message<?> message = MessageBuilder.withPayload(baseMQMessage)
                        .setHeader(MQLocalTransactionListener.MSG_HEADER_TRANSACTION_LISTENER, uniqueRocketMQLocalTransactionListener.name())
                        .setHeader("KEYS", traceId).build();
                TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction(topic, message, transactionObj);
                messageProduceContext.setSendResult(transactionSendResult.getSendStatus().name());
            } catch (Throwable e) {
                messageProduceContext.setSendResult("Throwable");
                messageProduceContext.setThrowable(e);
                throw e;
            }
        });
    }

    @Override
    public SendResult sendWithoutRetry(String topic, String hashKey, String baseMQMessage, String traceIdString) {
        Message<?> message = MessageBuilder.withPayload(baseMQMessage).setHeader("KEYS", traceIdString).build();
        SendResult sendResult = null;
        try {
            if (StringUtils.isNotBlank(hashKey)) {
                sendResult = rocketMQTemplate.syncSendOrderly(topic, message, hashKey);
            } else {
                sendResult = rocketMQTemplate.syncSend(topic, message);
            }
        } catch (Exception e) {
            if (Objects.isNull(sendResult)) {
                sendResult = new SendResult();
            }
            sendResult.setSendStatus(SendStatus.SLAVE_NOT_AVAILABLE);
            log.fatal("MQProducerImpl-retryMessage mq server unavailable : message = {}, topic = {}, traceIdString = {}", message, topic, traceIdString, e);
        }

        return sendResult;
    }

    private void failThenPersist(MQSendConfig mqSendConfig, String topic, String hashKey, String traceIdString, BaseMQMessage baseMQMessage) {
        if (Objects.nonNull(persistent) && Objects.nonNull(uniqueID) && mqSendConfig.getPersistence()) {
            String baseMQMessageJson = JsonUtil.toJSONString(baseMQMessage);
            MqFailLogEntity mqFailLogEntity = new MqFailLogEntity();
            mqFailLogEntity.setId(uniqueID.getUniqueId("remq"));
            mqFailLogEntity.setTopic(topic);
            if (!StringUtils.isNotBlank(hashKey)) {
                mqFailLogEntity.setHashKey(hashKey);
            }
            mqFailLogEntity.setTraceId(traceIdString);
            mqFailLogEntity.setBody(baseMQMessageJson);
            mqFailLogEntity.setSendConfig(JsonUtil.toJSONString(mqSendConfig));
            mqFailLogEntity.setRetryNum(rocketMQTemplate.getProducer().getRetryTimesWhenSendFailed());
            persistent.persistentMessage(mqFailLogEntity);
        }
    }
}
