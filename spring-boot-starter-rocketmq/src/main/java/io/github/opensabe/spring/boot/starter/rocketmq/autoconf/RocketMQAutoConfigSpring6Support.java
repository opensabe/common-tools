package io.github.opensabe.spring.boot.starter.rocketmq.autoconf;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 目前官方rocket.spring.boot.starter没有升级到spring6.x
 * 自动启动用的是spring.factories，到了spring 6.x就不支持这种启动方式
 * 等官方升级到Spring boot 3可以去掉该类
 */
@AutoConfiguration(before = RocketMQAutoConfiguration.class)
@Import(org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration.class)
public class RocketMQAutoConfigSpring6Support {
}
