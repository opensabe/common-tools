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
package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class SocketIOExecuteObservationConvention implements ObservationConvention<SocketIOExecuteContext> {
    public static SocketIOExecuteObservationConvention DEFAULT = new SocketIOExecuteObservationConvention();

    private final String TAG_SESSION_ID = "sessionId";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof SocketIOExecuteContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(SocketIOExecuteContext context) {
        return KeyValues.of("socketio", "execute");
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(SocketIOExecuteContext context) {
        return KeyValues.of(TAG_SESSION_ID, context.getSessionId());

    }
}
