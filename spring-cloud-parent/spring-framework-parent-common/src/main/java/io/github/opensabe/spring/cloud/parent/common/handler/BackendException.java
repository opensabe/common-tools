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

public class BackendException extends IException {

    public static <T> T resolveBaseResponse (BaseRsp<T> rsp) {
        return rsp.resolveData(BackendException::new);
    }

    public BackendException(Integer code, String message, String innerMessage, Object data) {
        super(code, message, innerMessage, data);
    }

    public BackendException(Integer code, String message, Object data) {
        super(code, message, data);
    }

    public BackendException(Integer code, String message, String innerMessage) {
        super(code, message, innerMessage);
    }

    public BackendException(Integer code, String message) {
        super(code, message);
    }

    public BackendException(ErrorMessage message, String innerMessage, Object data) {
        super(message, innerMessage, data);
    }

    public BackendException(ErrorMessage message, String innerMessage) {
        super(message, innerMessage);
    }

    public BackendException(ErrorMessage message, Object data) {
        super(message, data);
    }

    public BackendException(ErrorMessage message) {
        super(message);
    }
}
