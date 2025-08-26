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
package io.github.opensabe.common.web.config.exception;

import java.io.Serializable;

import io.github.opensabe.common.web.config.base.ErrResponse;
import io.github.opensabe.common.web.config.base.ErrorUtil;
import lombok.Getter;

/**
 * 17/8/17 下午7:31.
 *
 * @author zhaozhou
 */
public class RESTFullBaseException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 90291629754433147L;
    @Getter
    protected Integer code;

    RESTFullBaseException(String message) {
        super(message);
    }

    RESTFullBaseException(String message, int code) {
        super(message);
        this.code = code;
    }

    RESTFullBaseException(String msg, ErrResponse err) {
        super(ErrorUtil.appendError(msg, err));
        this.code = code;
    }
}
