package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.entity.base.vo.BaseMQMessage;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import io.github.opensabe.common.utils.json.JsonUtil;
import io.github.opensabe.spring.boot.starter.rocketmq.jfr.MessageConsume;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.TraceContext;
import jakarta.annotation.Nonnull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;

@Log4j2
public abstract class AbstractMQConsumer implements RocketMQListener<String>, ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    @Autowired
    protected Environment environment;
    
    protected String topic;

    //用来阻断消费，防止微服务 ApplicationContext 还没启动完全就开始消费
    private final CountDownLatch cdl = new CountDownLatch(1);

    //用来防止每次消费都要读取 cdl 导致性能下降
    private volatile boolean isStarted = false;

    @PostConstruct
    public void init() {
        RocketMQMessageListener rocketMQMessageListener = getClass().getAnnotation(RocketMQMessageListener.class);
        this.topic = environment.resolvePlaceholders(rocketMQMessageListener.topic());
    }

    @Override
    public void onMessage(String s) {
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
        //为了防止泛型丢失带来的JSON反序列化错误，都不用泛型，具体data的反序列化交给子类
        //        BaseMQMessage<T> tBaseMQMessage = JSON.parseObject(s, new TypeReference<>() {
        //            });
        // compressed messages will have `compressed.` prefix
        BaseMQMessage tBaseMQMessage = MQMessageUtil.decode(JsonUtil.parseObject(s, BaseMQMessage.class));
        Observation observation = unifiedObservationFactory.createEmptyObservation();
        TraceContext traceContext = UnifiedObservationFactory.getTraceContext(observation);

        //如果data为null，证明不是包装格式
        if (StringUtils.isBlank(tBaseMQMessage.getData())) {

            MessageConsume messageConsume = new MessageConsume(traceContext.traceId(), traceContext.traceId(), traceContext.spanId(), topic);
            messageConsume.begin();
            observation.observe(() -> {
                try  {
                    BaseMQMessage message = new BaseMQMessage();
//                tBaseMQMessage.setOpenTracingSpanIdLong(traceContext.spanId());
//                tBaseMQMessage.setOpenTracingTraceIdLongHigh(traceContext.traceIdHigh());
//                tBaseMQMessage.setOpenTracingTraceIdLong(traceContext.traceId());
                    message.setData(s);
                    log.info("AbstractMQConsumer-onMessage: topic: {} -> message: {}", topic, s);
                    try {
                        onBaseMQMessage(message);
                        messageConsume.setSuccessful(true);
                    } catch (Throwable e) {
                        log.error("MQ consume failed! {}, {}", s, e.getMessage(), e);
                        messageConsume.setSuccessful(false);
                        messageConsume.setThrowable(e);
                        throw e;
                    }
                } finally {
                    messageConsume.commit();
                }
            });

        } else {
            //由于保持 rocket mq 的 traceId 会导致基于 traceId 的重试效果极差，并且，同一个 traceId 的日志过多，所以这里仅仅是在日志里面记录一下初始发消息的 traceId
            MessageConsume messageConsume = new MessageConsume(tBaseMQMessage.getTraceId(), traceContext.traceId(), traceContext.spanId(), topic);
            messageConsume.begin();
            observation.observe(() -> {
                try {
                    log.info("AbstractMQConsumer-onMessage: topic: initial trace id {}, topic: {} -> message: {}", tBaseMQMessage.getTraceId(), topic, s);
                    try {
                        onBaseMQMessage(tBaseMQMessage);
                        messageConsume.setSuccessful(true);
                    } catch (Throwable e) {
                        log.error("MQ consume failed! {}, {}", s, e.getMessage(), e);
                        messageConsume.setSuccessful(false);
                        messageConsume.setThrowable(e);
                        throw e;
                    }
                } finally {
                    messageConsume.commit();
                }
            });
        }
    }
    
    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
        cdl.countDown();
        isStarted = true;
    }

    abstract protected void onBaseMQMessage(BaseMQMessage baseMQMessage);
}
