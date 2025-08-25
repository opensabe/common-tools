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

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class MessageConsumeObservationConvention implements ObservationConvention<MessageConsumeContext> {
    public static final MessageConsumeObservationConvention DEFAULT = new MessageConsumeObservationConvention();

    @Override
    public KeyValues getLowCardinalityKeyValues(MessageConsumeContext context) {
        return KeyValues.of(
                RocketMQObservationDocumentation.MESSAGE_CONSUME_TAG.TOPIC.withValue(context.getTopic()),
                RocketMQObservationDocumentation.MESSAGE_CONSUME_TAG.SUCCESSFUL.withValue(String.valueOf(context.getSuccessful()))
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(MessageConsumeContext context) {
        return KeyValues.of(
                RocketMQObservationDocumentation.MESSAGE_CONSUME_TAG.ORIGIN_TRACE_ID.withValue(context.getOriginTraceId()),
                RocketMQObservationDocumentation.MESSAGE_CONSUME_TAG.TOPIC.withValue(context.getTopic()),
                RocketMQObservationDocumentation.MESSAGE_CONSUME_TAG.SUCCESSFUL.withValue(String.valueOf(context.getSuccessful())),
                RocketMQObservationDocumentation.MESSAGE_CONSUME_TAG.THROWABLE.withValue(context.getThrowable() == null ? "" : context.getThrowable().getMessage())
        );
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof MessageConsumeContext;
    }
}
