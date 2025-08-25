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
package io.github.opensabe.spring.cloud.parent.web.common.handler;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.opensabe.base.RespUtil;
import io.github.opensabe.base.vo.BaseRsp;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;


@Log4j2
@Order
@RestControllerAdvice
public class ThrowableHandler {
    @ExceptionHandler(Throwable.class)
    public BaseRsp onThrowable(Throwable e, HttpServletRequest request) {
        var path = request.getRequestURI();
        log.error("{} error {}", path, e.getMessage(), e);
        return RespUtil.error("Sorry,something went wrong. Please try again later.");
    }
}
