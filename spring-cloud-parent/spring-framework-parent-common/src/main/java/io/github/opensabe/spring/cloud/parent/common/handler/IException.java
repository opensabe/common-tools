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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IException extends RuntimeException {
    private final Integer code;
    private final Object data;
    private final String innerMessage;

    public IException(Integer code, String message, String innerMessage, Object data) {
        super(message);
        this.code = code;
        this.data = data;
        this.innerMessage = innerMessage;
    }

    public IException(Integer code, String message, Object data) {
        this(code, message, null, data);
    }

    public IException(Integer code, String message, String innerMessage) {
        this(code, message, innerMessage, null);
    }

    public IException(Integer code, String message) {
        this(code, message, null, null);
    }

    public IException(ErrorMessage message, String innerMessage, Object data) {
        this(message.code(), message.message(), innerMessage, data);
    }

    public IException(ErrorMessage message, String innerMessage) {
        this(message, innerMessage, message.data());
    }

    public IException(ErrorMessage message, Object data) {
        this(message, null, data);
    }

    public IException(ErrorMessage message) {
        this(message, null, message.data());
    }
}
