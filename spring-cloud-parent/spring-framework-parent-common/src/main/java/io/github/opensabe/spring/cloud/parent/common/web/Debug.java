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
package io.github.opensabe.spring.cloud.parent.common.web;

import lombok.Getter;

/**
 * 全局 debug 开关：控制异常响应等是否向客户端暴露详细错误信息。
 */
public class Debug {
    /** 是否开启 debug 模式。 */
    @Getter
    private final boolean enabled;

    /**
     * @param enabled 是否开启 debug
     */
    public Debug(boolean enabled) {
        this.enabled = enabled;
    }
}
