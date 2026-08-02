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
package io.github.opensabe.spring.cloud.parent.web.common.misc;

/**
 * 框架内部使用的特殊 HTTP 状态码。
 * <p>
 * 用于在 Feign 客户端层表达断路器、Bulkhead 与 IO 异常等语义，供 {@link io.github.opensabe.spring.cloud.parent.web.common.feign.DefaultErrorDecoder} 判定是否重试。
 */
public enum SpecialHttpStatus {
    /**
     * 断路器打开
     */
    CIRCUIT_BREAKER_ON(581),
    /**
     * 可以重试的异常
     */
    RETRYABLE_IO_EXCEPTION(582),
    /**
     * 不能重试的异常
     */
    NOT_RETRYABLE_IO_EXCEPTION(583),
    /**
     * 超过限流限制
     */
    BULKHEAD_FULL(584),
    ;
    /**
     * 对应的 HTTP 状态码数值。
     */
    private int value;

    SpecialHttpStatus(int value) {
        this.value = value;
    }

    /**
     * 返回状态码数值。
     *
     * @return HTTP 状态码
     */
    public int getValue() {
        return value;
    }
}
