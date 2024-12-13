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
    }
    ;

    public enum MESSAGE_PRODUCE_TAG implements KeyName {
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

    public enum MESSAGE_CONSUME_TAG implements KeyName {
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
