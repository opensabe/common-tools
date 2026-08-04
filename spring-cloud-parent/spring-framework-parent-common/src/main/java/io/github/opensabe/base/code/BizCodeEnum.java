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
package io.github.opensabe.base.code;

import io.github.opensabe.spring.cloud.parent.common.handler.ErrorMessage;
import lombok.Getter;

/**
 * 标准 API 响应业务码枚举，实现 {@link ErrorMessage} 供异常与响应体共用。
 * <p>
 * 业务自定义码建议按 {@code SrcEnum.id + 4 digits} 规则扩展。
 */
@Getter
public enum BizCodeEnum implements ErrorMessage {
    /** 业务成功，默认成功响应码。 */
    SUCCESS(10000, "success"),
    /** 可预期的业务失败（区别于系统错误）。 */
    FAIL(11000, "fail"),
    /** 请求参数非法或校验失败。 */
    INVALID(19000, "invalid"),
    /** 资源未找到，常用于资源操作场景。 */
    RESOURCE_NOT_FOUND(19001, "resource not found"),
    /** 状态非法，如违反业务状态机约束。 */
    BAD_STATE(19002, "bad state"),
    /** 无权限访问。 */
    FORBIDDEN(19003, "Forbidden"),
    /** 非预期的系统错误。 */
    ERROR(19999, "error"),
    ;

    /** 业务码数值。 */
    private int val;
    /** 默认英文提示文案。 */
    private String defaultMsg;

    /**
     * @param val        业务码数值
     * @param defaultMsg 默认提示文案
     */
    BizCodeEnum(int val, String defaultMsg) {
        this.val = val;
        this.defaultMsg = defaultMsg;
    }

    @Override
    public int code() {
        return val;
    }

    @Override
    public String message() {
        return defaultMsg;
    }
}
