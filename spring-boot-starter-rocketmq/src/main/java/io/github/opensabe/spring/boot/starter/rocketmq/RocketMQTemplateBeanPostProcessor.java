package io.github.opensabe.spring.boot.starter.rocketmq;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.executor.ThreadPoolFactory;
import jakarta.annotation.Nonnull;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.ExecutorService;

@Log4j2
public class RocketMQTemplateBeanPostProcessor implements BeanPostProcessor {
    @Autowired
    private ThreadPoolFactory threadPoolFactory;
    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (bean instanceof RocketMQTemplate rocketMQTemplate) {
            DefaultMQProducer producer = rocketMQTemplate.getProducer();
            ExecutorService callbackExecutor = threadPoolFactory.createNormalThreadPool("RocketMQTemplateCallBackExecutor-" + beanName, 32);
            ExecutorService asyncSenderExecutor = threadPoolFactory.createNormalThreadPool("RocketMQTemplateAsyncSenderExecutor-" + beanName, 32);
            producer.setCallbackExecutor(callbackExecutor);
            producer.setAsyncSenderExecutor(asyncSenderExecutor);
            // 设置超时时间为5秒
            producer.setMqClientApiTimeout(5 * 1000);
            log.info("RocketMQTemplateBeanPostProcessor-postProcessAfterInitialization: {} producer: {}", beanName, JSON.toJSONString(producer));

        }
        return bean;
    }
}
