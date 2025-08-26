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
 * Basic MQ message format
 */
@Getter
@Setter
public class BaseMsg<T> {
    private String traceId;                         // traceId for auditing
    private Long ts;                                // current system timestamp
    private String src;                             // src system SrcEnum.val
    private String action;                          // customized action
    private T data;                                 // content data
}