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
package io.github.opensabe.common.mybatis.jfr;

import io.github.opensabe.common.mybatis.observation.ConnectionContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

/**
 * mysql连接池JFR事件，创建连接和关闭连接时上报
 *
 * @author maheng
 */
@Getter
@Setter
@Category({"observation", "mybatis"})
@Label("Connection Pool Monitor")
@StackTrace(value = false)
public class ConnectionEvent extends Event {

    /**
     * 连接创建时间
     */
    @Label("Connection Create Time")
    private final long connectedTime;

    /**
     * 连接池中剩余连接数量
     */
    @Label("Connection Count")
    private final int remain;

    @Label("Connect Event Type")
    private final String type;

    private final boolean success;

    public ConnectionEvent(ConnectionContext context) {
        this.connectedTime = context.getConnectedTime();
        this.remain = context.getActiveCount();
        this.type = context.getEvent();
        this.success = context.isSuccess();
    }
}
