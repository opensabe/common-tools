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
package io.github.opensabe.spring.cloud.parent.common.handler;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.opensabe.base.RespUtil;
import io.github.opensabe.base.vo.BaseRsp;
import io.github.opensabe.spring.cloud.parent.common.web.Debug;
import io.github.opensabe.spring.cloud.parent.common.web.Path;
import lombok.extern.log4j.Log4j2;


/**
 * 兜底 {@link Throwable} 处理器：捕获未被其他 handler 处理的异常并返回统一错误响应。
 * <p>
 * 非 debug 模式下不向客户端暴露异常详情。
 */
@Log4j2
@Order
@RestControllerAdvice
public class ThrowableHandler {

    private final Debug debug;

    /**
     * @param debug 调试开关，控制是否向客户端返回异常 message
     */
    public ThrowableHandler(Debug debug) {
        this.debug = debug;
    }

    /**
     * 处理任意未捕获异常。
     *
     * @param e    异常
     * @param path 请求路径（{@link Path} 注入）
     * @return 系统错误响应
     */
    @ExceptionHandler(Throwable.class)
    public BaseRsp<Void> onThrowable(Throwable e, @Path String path) {
        log.error("{} error {}", path, e.getMessage(), e);
        String msg = debug.isEnabled() ? e.getMessage() : null;
        return RespUtil.error(msg, "Sorry,something went wrong. Please try again later.");
    }
}
