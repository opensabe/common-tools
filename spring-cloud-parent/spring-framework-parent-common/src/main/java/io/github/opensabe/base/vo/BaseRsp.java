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
package io.github.opensabe.base.vo;

import java.util.Objects;
import java.util.function.BiFunction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.github.opensabe.base.code.BizCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 HTTP/API 响应体格式。
 *
 * @param <T> 业务数据载荷类型
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseRsp<T> {

    /** 业务响应码，对应 {@link BizCodeEnum#getVal()}。 */
    @Schema(example = "10000")
    private int bizCode;

    /** 系统/内部级说明，通常不直接暴露给终端用户。 */
    private String innerMsg;

    /** 用户可见提示消息。 */
    @Schema(example = "success")
    private String message;

    /** 业务数据载荷。 */
    private T data;

    /**
     * 判断是否为成功响应（业务码等于 {@link BizCodeEnum#SUCCESS}）。
     *
     * @return {@code true} 表示成功
     */
    @JsonIgnore
    public boolean isSuccess() {
        return Objects.equals(bizCode, BizCodeEnum.SUCCESS.getVal());
    }

    /**
     * 成功时返回 {@link #data}；失败时通过 {@code supplier} 构造并抛出运行时异常。
     *
     * @param supplier 失败时接收 {@code (bizCode, message)} 并返回异常的函数
     * @return 成功时的业务数据
     * @param <E> 抛出的异常类型
     */
    @JsonIgnore
    public <E extends RuntimeException> T resolveData(BiFunction<Integer, String, E> supplier) {
        if (isSuccess()) {
            return data;
        }
        throw supplier.apply(bizCode, message);
    }
}
