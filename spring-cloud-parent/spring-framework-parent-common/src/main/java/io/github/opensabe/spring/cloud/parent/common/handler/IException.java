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

/**
 * 带业务码与可选载荷的运行时异常基类，{@link FrontendException} 与 {@link BackendException} 均继承本类。
 */
@Getter
@Setter
public class IException extends RuntimeException {
    /** 业务错误码。 */
    private final Integer code;
    /** 可选附加数据，会写入 {@link io.github.opensabe.base.vo.BaseRsp#getData()}。 */
    private final Object data;
    /** 内部/系统级说明。 */
    private final String innerMessage;

    /**
     * @param code         业务码
     * @param message      用户可见消息（异常 {@link #getMessage()}）
     * @param innerMessage 内部说明
     * @param data         附加数据
     */
    public IException(Integer code, String message, String innerMessage, Object data) {
        super(message);
        this.code = code;
        this.data = data;
        this.innerMessage = innerMessage;
    }

    /**
     * @param code    业务码
     * @param message 用户可见消息
     * @param data    附加数据
     */
    public IException(Integer code, String message, Object data) {
        this(code, message, null, data);
    }

    /**
     * @param code         业务码
     * @param message      用户可见消息
     * @param innerMessage 内部说明
     */
    public IException(Integer code, String message, String innerMessage) {
        this(code, message, innerMessage, null);
    }

    /**
     * @param code    业务码
     * @param message 用户可见消息
     */
    public IException(Integer code, String message) {
        this(code, message, null, null);
    }

    /**
     * @param message      错误码枚举
     * @param innerMessage 内部说明
     * @param data         附加数据
     */
    public IException(ErrorMessage message, String innerMessage, Object data) {
        this(message.code(), message.message(), innerMessage, data);
    }

    /**
     * @param message      错误码枚举
     * @param innerMessage 内部说明
     */
    public IException(ErrorMessage message, String innerMessage) {
        this(message, innerMessage, message.data());
    }

    /**
     * @param message 错误码枚举
     * @param data    附加数据
     */
    public IException(ErrorMessage message, Object data) {
        this(message, null, data);
    }

    /**
     * @param message 错误码枚举
     */
    public IException(ErrorMessage message) {
        this(message, null, message.data());
    }
}
