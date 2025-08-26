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
package io.github.opensabe.common.web.config.base;

import lombok.Data;

/**
 * 17/9/28 下午12:25.
 *
 * @author zhaozhou
 */
@Data
public class ErrResponse<T> {
    private String message;
    private T data;

    public ErrResponse() {
    }

    public ErrResponse(T data) {
        this.data = data;
    }
}
