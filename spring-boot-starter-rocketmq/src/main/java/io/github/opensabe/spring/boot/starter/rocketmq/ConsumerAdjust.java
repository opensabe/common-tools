package io.github.opensabe.spring.boot.starter.rocketmq;

import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

/**
 *  以前ConsumeFromWhere是写在注解里，这样得修改源码，侵入性比较强，不利于日后升级
 *  所以采用BeanPostProcessor的方式来扩展，如果要设置ConsumeFromWhere，只需要将
 *  我们的Consumer实现ConsumerAdjust接口即可
 * @author heng.ma
 */
public interface ConsumerAdjust {

    /**
     * @see org.apache.rocketmq.client.consumer.DefaultMQPushConsumer#setConsumeFromWhere(ConsumeFromWhere) 
     */
    ConsumeFromWhere consumeFromWhere ();


    /**
     * 当 consumeFromWhere = {@link ConsumeFromWhere#CONSUME_FROM_TIMESTAMP} 时有效，
     * 只消费 {consumeFromSecondsAgo} 秒之前的消息
     * @see org.apache.rocketmq.client.consumer.DefaultMQPushConsumer#setConsumeTimestamp(String)
     * @return
     */
    long consumeFromSecondsAgo();
}
