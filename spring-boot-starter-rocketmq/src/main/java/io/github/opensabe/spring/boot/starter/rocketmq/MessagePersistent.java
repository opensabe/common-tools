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

import io.github.opensabe.common.config.dal.db.entity.MqFailLogEntity;
import jakarta.annotation.Nonnull;

/**
 * 消息发送失败以后，保存到数据库，taskCenter重试。
 * 为了跟mybatis解耦，这里抽象一层，原来的Mapper包含Mybatis注解，不具有通用性，
 * 后续jdbc完善多数据源以后，在这里也添加jdbc支持
 *
 * @author heng.ma
 */
public interface MessagePersistent {

    void persistentMessage(@Nonnull MqFailLogEntity entity);
}
