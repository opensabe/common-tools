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
package io.github.opensabe.spring.boot.starter.socketio;

import io.github.opensabe.base.code.BizCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseAck<T> {
    /**
     * BizCode
     *
     * @see BizCodeEnum
     */
    private Integer b;
    /**
     * msg
     */
    private String m;
    /**
     * payLoad
     */
    private T d;
    /**
     * flag
     * 请求标识,前端传入，后端直接返回，前端根据此标识找到对应的异步响应
     */
    private String f;
}
