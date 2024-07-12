package io.github.opensabe.common.buffer.observation;

import io.micrometer.observation.docs.ObservationDocumentation;

/**
 * BatchBufferQueueBatchManipulateObservationDocumentation
 * 不需要什么额外的信息，所以这里直接使用默认的空实现
 * 会自动添加计时的相关 Value（由于 DefaultMeterObservationHandler 的存在）
 * @see io.micrometer.core.instrument.observation.DefaultMeterObservationHandler
 */
public enum BatchBufferQueueBatchManipulateObservationDocumentation implements ObservationDocumentation {
    DEFAULT_OBSERVATION_DOCUMENTATION {
        @Override
        public String getName() {
            return "opensabe.batch-buffer-queue.batch-manipulate";
        }
    },
    ;
}
