package io.github.opensabe.common.alive.client;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.alive.client.message.MessageVo;
import io.github.opensabe.common.alive.client.message.Publish;
import io.github.opensabe.common.alive.client.message.Response;
import io.github.opensabe.common.alive.client.message.enumeration.MQTopic;
import io.github.opensabe.common.alive.client.message.enumeration.PushType;
import io.github.opensabe.common.alive.client.message.enumeration.RetCode;
import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.spring.boot.starter.rocketmq.jfr.MessageProduce;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
        this.producer.asyncSend(this.getTopic(messageVo), baseMQMessage, new SendCallback() {
            public void onSuccess(SendResult sendResult) {
                callback.opComplete(Set.of(Response.builder().retCode(RetCode.SUCCESS).requestId(message.getRequestId()).build()));
            }

            public void onException(Throwable throwable) {
                MQClientImpl.log.error(throwable);
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
        wrapTraceInfo(baseMQMessage,message.getTopic());
        baseMQMessage.setTs(System.currentTimeMillis());
        baseMQMessage.setData(JSON.toJSONString(message));
        baseMQMessage.setAction("default");
        return baseMQMessage;
    }

    private void wrapTraceInfo(BaseMQMessage baseMQMessage,String topic){
        try{
            Observation observation = unifiedObservationFactory.getCurrentOrCreateEmptyObservation();
            TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);
            final MessageProduce messageProduceJfrEvent = new MessageProduce(traceContext.traceId(),
                    traceContext.spanId(), topic);
            messageProduceJfrEvent.begin();
            baseMQMessage.setTraceId(messageProduceJfrEvent.getTraceId());
            baseMQMessage.setSpanId(messageProduceJfrEvent.getSpanId());
        }catch (Throwable throwable){
            log.warn("MQClientImpl-wrapMessage set traceId failed {} ",throwable.getMessage(),throwable);
        }
    }
}