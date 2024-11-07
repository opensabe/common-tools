package io.github.opensabe.spring.boot.starter.rocketmq;

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;

/**
 * 消息发送失败以后，保存到数据库，taskCenter重试。
 * 为了跟mybatis解耦，这里抽象一层，原来的Mapper包含Mybatis注解，不具有通用性，
 * 后续jdbc完善多数据源以后，在这里也添加jdbc支持
 * @author heng.ma
 */
public interface MessagePersistent {

    void persistent (MqFailLogEntity entity);
}
