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

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum RocketMQObservationDocumentation implements ObservationDocumentation {
    PRODUCE {
        @Override
        public String getName() {
            return "rocketmq.message.produce";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return MessageProduceObservationConvention.class;
        }
    },
    CONSUME {
        @Override
        public String getName() {
            return "rocketmq.message.consume";
        }

        @Override
        public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
            return MessageConsumeObservationConvention.class;
        }
    };

    public enum MessageProduceTag implements KeyName {
        TOPIC {
            @Override
            public String asString() {
                return "message.produce.topic";
            }
        },
        MSG_LENGTH {
            @Override
            public String asString() {
                return "message.produce.msg.length";
            }
        },
        SEND_RESULT {
            @Override
            public String asString() {
                return "message.produce.send.result";
            }
        },
        THROWABLE {
            @Override
            public String asString() {
                return "message.produce.throwable";
            }
        },
        ;
    }

    public enum MessageConsumeTag implements KeyName {
        ORIGIN_TRACE_ID {
            @Override
            public String asString() {
                return "message.consume.origin.trace.id";
            }
        },
        TOPIC {
            @Override
            public String asString() {
                return "message.consume.topic";
            }
        },
        SUCCESSFUL {
            @Override
            public String asString() {
                return "message.consume.successful";
            }
        },
        THROWABLE {
            @Override
            public String asString() {
                return "message.consume.throwable";
            }
        },
        ;
    }
}
