package io.github.opensabe.spring.boot.starter.socketio.tracing.common;

import com.alibaba.fastjson.JSON;
import io.github.opensabe.common.utils.OptionalUtil;

public interface SocketIOParam extends SocketIOCommonEvent{

    default String getAllHeaders() {
        return OptionalUtil.orNull(()-> JSON.toJSONString(currentHttpHeaders().entries()));
    }

    default String getSessionId() {
        return currentSocketIOClient().getSessionId().toString();
    }

}
