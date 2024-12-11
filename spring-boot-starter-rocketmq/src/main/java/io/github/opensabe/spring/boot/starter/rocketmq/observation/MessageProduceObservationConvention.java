package io.github.opensabe.spring.boot.starter.rocketmq.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class MessageProduceObservationConvention implements ObservationConvention<MessageProduceContext> {
    public static final MessageProduceObservationConvention DEFAULT = new MessageProduceObservationConvention();
    @Override
    public KeyValues getLowCardinalityKeyValues(MessageProduceContext context) {
        return KeyValues.of(
                RocketMQObservationDocumentation.MESSAGE_PRODUCE_TAG.SEND_RESULT.withValue(context.getSendResult()),
                RocketMQObservationDocumentation.MESSAGE_PRODUCE_TAG.TOPIC.withValue(context.getTopic())
        );
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(MessageProduceContext context) {
        return KeyValues.of(
                RocketMQObservationDocumentation.MESSAGE_PRODUCE_TAG.SEND_RESULT.withValue(context.getSendResult()),
                RocketMQObservationDocumentation.MESSAGE_PRODUCE_TAG.MSG_LENGTH.withValue(String.valueOf(context.getMsgLength())),
                RocketMQObservationDocumentation.MESSAGE_PRODUCE_TAG.TOPIC.withValue(context.getTopic()),
                RocketMQObservationDocumentation.MESSAGE_PRODUCE_TAG.THROWABLE.withValue(context.getThrowable() == null ? "" : context.getThrowable().getMessage())
        );
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof MessageProduceContext;
    }
}
