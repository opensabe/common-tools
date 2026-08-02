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

/**
 * 面向终端用户的业务异常，{@link #getMessage()} 作为 i18n 消息模板键。
 * <p>
 * 由 {@link GexceptionHandler} 捕获并通过 {@link I18nMessageResolver} 解析用户可见文案。
 */
@Getter
@SuppressWarnings("unused")
public class FrontendException extends IException {

    /** 国际化消息占位符参数。 */
    private final Object[] args;

    /**
     * @param code         业务码
     * @param message      i18n 消息模板键
     * @param innerMessage 内部说明
     * @param data         附加数据
     * @param args         i18n 占位符参数
     */
    public FrontendException(Integer code, String message, String innerMessage, Object data, Object... args) {
        super(code, message, innerMessage, data);
        this.args = args;
    }

    /**
     * @param code    业务码
     * @param message i18n 消息模板键
     * @param data    附加数据
     * @param args    i18n 占位符参数
     */
    public FrontendException(Integer code, String message, Object data, Object... args) {
        this(code, message, null, data, args);
    }

    /**
     * @param code         业务码
     * @param message      i18n 消息模板键
     * @param innerMessage 内部说明
     * @param args         i18n 占位符参数
     */
    public FrontendException(Integer code, String message, String innerMessage, Object... args) {
        this(code, message, innerMessage, null, args);
    }

    /**
     * @param code    业务码
     * @param message i18n 消息模板键
     * @param args    i18n 占位符参数
     */
    public FrontendException(Integer code, String message, Object... args) {
        this(code, message, null, null, args);
    }

    /**
     * @param message      错误码枚举
     * @param innerMessage 内部说明
     * @param data         附加数据
     * @param args         i18n 占位符参数
     */
    public FrontendException(ErrorMessage message, String innerMessage, Object data, Object... args) {
        super(message, innerMessage, data);
        this.args = args;
    }

    /**
     * @param message      错误码枚举
     * @param innerMessage 内部说明
     * @param args         i18n 占位符参数
     */
    public FrontendException(ErrorMessage message, String innerMessage, Object... args) {
        this(message, innerMessage, message.data(), args);
    }

    /**
     * @param message 错误码枚举
     * @param data    附加数据
     * @param args    i18n 占位符参数
     */
    public FrontendException(ErrorMessage message, Object data, Object... args) {
        this(message, null, data, args);
    }

    /**
     * @param message 错误码枚举
     * @param args    i18n 占位符参数
     */
    public FrontendException(ErrorMessage message, Object... args) {
        this(message, message.data(), args);
    }

    /**
     * 解析 {@link BaseRsp}：成功返回 data，失败抛出 {@link FrontendException}。
     *
     * @param rsp 远程/下游响应
     * @param <T> 数据类型
     * @return 成功时的 data
     */
    public static <T> T resolveBaseResponse(BaseRsp<T> rsp) {
        return rsp.resolveData(FrontendException::new);
    }

}
