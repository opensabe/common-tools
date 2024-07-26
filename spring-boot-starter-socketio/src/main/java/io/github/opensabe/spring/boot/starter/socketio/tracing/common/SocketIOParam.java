package io.github.opensabe.spring.boot.starter.socketio.tracing.common;

import io.github.opensabe.common.utils.OptionalUtil;
import io.github.opensabe.common.utils.json.JsonUtil;

public interface SocketIOParam extends SocketIOCommonEvent{

    default String getAllHeaders() {
        return OptionalUtil.orNull(()-> JsonUtil.toJSONString(currentHttpHeaders().entries()));
    }

    default String getSessionId() {
        return currentSocketIOClient().getSessionId().toString();
    }

}
