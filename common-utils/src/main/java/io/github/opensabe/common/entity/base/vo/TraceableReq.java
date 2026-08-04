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
package io.github.opensabe.common.entity.base.vo;


import lombok.Getter;
import lombok.Setter;

/**
 * 可追踪请求基类，携带来源系统、traceId 与时间戳。
 */
@Getter
@Setter
public class TraceableReq {
    /** 来源系统标识（{@code SrcEnum.val}）。 */
    private String src;
    /** 审计用 traceId。 */
    private String traceId;
    /** 当前系统时间戳（毫秒）。 */
    private long ts;
}