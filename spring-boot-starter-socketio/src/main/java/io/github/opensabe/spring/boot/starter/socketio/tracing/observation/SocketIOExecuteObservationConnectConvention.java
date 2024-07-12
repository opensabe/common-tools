package io.github.opensabe.spring.boot.starter.socketio.tracing.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;

public class SocketIOExecuteObservationConnectConvention implements ObservationConvention<SocketIOExecuteContext> {
    public static SocketIOExecuteObservationConnectConvention DEFAULT = new SocketIOExecuteObservationConnectConvention();

    private final String TAG_SESSION_ID = "sessionId";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof SocketIOExecuteContext;
    }

    @Override
    public KeyValues getLowCardinalityKeyValues(SocketIOExecuteContext context) {
        return KeyValues.of(TAG_SESSION_ID, context.getSessionId());
    }

    @Override
    public KeyValues getHighCardinalityKeyValues(SocketIOExecuteContext context) {
        return KeyValues.of(TAG_SESSION_ID, context.getSessionId());
    }
}
