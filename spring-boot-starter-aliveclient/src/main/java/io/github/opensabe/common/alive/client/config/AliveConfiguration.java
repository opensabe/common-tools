package io.github.opensabe.common.alive.client.config;

import com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter;
import io.github.opensabe.common.alive.client.Client;
import io.github.opensabe.common.alive.client.MQClientImpl;
import io.github.opensabe.common.observation.UnifiedObservationFactory;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static io.github.opensabe.common.alive.client.config.AliveProperties.ROCKET_CLIENT_NAME;


//@Log4j2
@Configuration(proxyBeanMethods = false)
public class AliveConfiguration {
    @Autowired
    private AliveProperties aliveProperties;
    @Autowired
    private UnifiedObservationFactory unifiedObservationFactory;
    @Primary
    @Bean(ROCKET_CLIENT_NAME)
    @ConditionalOnProperty(prefix = "alive.push",name = "rocketmq.name-server")
    public Client rocketAliveClient () {
         var producer = new RocketMQAutoConfiguration().
                defaultMQProducer(aliveProperties.getRocketmq());
        producer.setInstanceName("aliveProducer");
        try {
            producer.start();
        } catch (MQClientException e) {
            throw new BeanDefinitionValidationException(String.format("Failed to startup MQProducer for RocketMQTemplate {}",ROCKET_CLIENT_NAME), e);
        }
        var template = new RocketMQTemplate();
        template.setProducer(producer);
        template.setMessageConverter(new MappingFastJsonMessageConverter());
        return new MQClientImpl(template,aliveProperties.getProduct(),unifiedObservationFactory);
    }

}
