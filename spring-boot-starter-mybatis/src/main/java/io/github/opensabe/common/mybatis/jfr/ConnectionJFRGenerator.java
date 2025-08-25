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

import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.common.mybatis.observation.ConnectionContext;

/**
 * mysql连接池上报JFR
 *
 * @author maheng
 */
public class ConnectionJFRGenerator extends ObservationToJFRGenerator<ConnectionContext> {
    @Override
    public Class<ConnectionContext> getContextClazz() {
        return ConnectionContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(ConnectionContext context) {
        return context.containsKey(ConnectionEvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(ConnectionContext context) {
        return true;
    }

    @Override
    protected void commitOnStop(ConnectionContext context) {
        ConnectionEvent event = context.get(ConnectionEvent.class);
        event.commit();
    }

    @Override
    protected void generateOnStart(ConnectionContext context) {
        ConnectionEvent event = new ConnectionEvent(context);
        context.put(ConnectionEvent.class, event);
        event.begin();
    }
}
