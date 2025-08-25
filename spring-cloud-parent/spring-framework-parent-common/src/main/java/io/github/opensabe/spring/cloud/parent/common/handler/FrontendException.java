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

import io.github.opensabe.base.vo.BaseRsp;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class FrontendException extends IException {

    public static <T> T resolveBaseResponse (BaseRsp<T> rsp) {
        return rsp.resolveData(FrontendException::new);
    }

    /**
     * 国家化占位符
     */
    private final Object [] args;

    public FrontendException(Integer code, String message, String innerMessage, Object data, Object ... args) {
        super(code, message, innerMessage, data);
        this.args = args;
    }

    public FrontendException(Integer code, String message, Object data, Object ... args) {
        this(code, message, null, data, args);
    }

    public FrontendException(Integer code, String message, String innerMessage, Object ... args) {
        this(code, message, innerMessage, null, args);
    }

    public FrontendException(Integer code, String message, Object ... args) {
        this(code, message, null, null, args);
    }

    public FrontendException(ErrorMessage message, String innerMessage, Object data, Object ... args) {
        super(message, innerMessage, data);
        this.args = args;
    }

    public FrontendException(ErrorMessage message, String innerMessage, Object ... args) {
        this(message, innerMessage, message.data(), args);
    }

    public FrontendException(ErrorMessage message, Object data, Object ... args) {
        this(message, null, data, args);
    }

    public FrontendException(ErrorMessage message, Object ... args) {
        this(message, message.data(), args);
    }

}
