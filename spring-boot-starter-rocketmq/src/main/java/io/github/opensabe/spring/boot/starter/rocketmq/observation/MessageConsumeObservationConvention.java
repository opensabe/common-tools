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
