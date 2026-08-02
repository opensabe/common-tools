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

/**
 * 面向管理后台/内部服务的业务异常，消息不做国际化处理。
 * <p>
 * 由 {@link GexceptionHandler} 捕获并转换为 {@link BaseRsp}。
 */
public class BackendException extends IException {

    /**
     * @param code         业务码
     * @param message      用户可见消息
     * @param innerMessage 内部说明
     * @param data         附加数据
     */
    public BackendException(Integer code, String message, String innerMessage, Object data) {
        super(code, message, innerMessage, data);
    }

    /**
     * @param code    业务码
     * @param message 用户可见消息
     * @param data    附加数据
     */
    public BackendException(Integer code, String message, Object data) {
        super(code, message, data);
    }

    /**
     * @param code         业务码
     * @param message      用户可见消息
     * @param innerMessage 内部说明
     */
    public BackendException(Integer code, String message, String innerMessage) {
        super(code, message, innerMessage);
    }

    /**
     * @param code    业务码
     * @param message 用户可见消息
     */
    public BackendException(Integer code, String message) {
        super(code, message);
    }

    /**
     * @param message      错误码枚举
     * @param innerMessage 内部说明
     * @param data         附加数据
     */
    public BackendException(ErrorMessage message, String innerMessage, Object data) {
        super(message, innerMessage, data);
    }

    /**
     * @param message      错误码枚举
     * @param innerMessage 内部说明
     */
    public BackendException(ErrorMessage message, String innerMessage) {
        super(message, innerMessage);
    }

    /**
     * @param message 错误码枚举
     * @param data    附加数据
     */
    public BackendException(ErrorMessage message, Object data) {
        super(message, data);
    }

    /**
     * @param message 错误码枚举
     */
    public BackendException(ErrorMessage message) {
        super(message);
    }

    /**
     * 解析 {@link BaseRsp}：成功返回 data，失败抛出 {@link BackendException}。
     *
     * @param rsp 远程/下游响应
     * @param <T> 数据类型
     * @return 成功时的 data
     */
    public static <T> T resolveBaseResponse(BaseRsp<T> rsp) {
        return rsp.resolveData(BackendException::new);
    }
}
