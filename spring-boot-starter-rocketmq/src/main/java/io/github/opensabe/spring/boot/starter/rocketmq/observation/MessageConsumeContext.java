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
package io.github.opensabe.spring.boot.starter.rocketmq.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageConsumeContext extends Observation.Context {
    private final String originTraceId;
    private final String topic;
    private Boolean successful = false;
    private Throwable throwable;

    public MessageConsumeContext(String originTraceId, String topic) {
        this.originTraceId = originTraceId == null ? "" : originTraceId;
        this.topic = topic == null ? "" : topic;
    }
}
