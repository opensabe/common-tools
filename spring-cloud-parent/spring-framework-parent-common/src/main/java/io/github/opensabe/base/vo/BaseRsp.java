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
 * Basic response format
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseRsp<T> {

    @Schema(example = "10000")
    private int bizCode;                // response code RspCodeEnum.val

    //考虑这里是否要加JsonIgnore
    private String innerMsg;            // response msg in systematic level

    @Schema(example = "success")
    private String message;             // response user msg

    private T data;                     // response content data

    @JsonIgnore
    public boolean isSuccess() {
        return Objects.equals(bizCode, BizCodeEnum.SUCCESS.getVal());
    }

    @JsonIgnore
    public T resolveData(BiFunction<Integer, String, RuntimeException> supplier) {
        if (isSuccess()) {
            return data;
        }
        throw supplier.apply(bizCode, message);
    }
}