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
package io.github.opensabe.common.redisson.observation.ratelimiter;

import java.util.concurrent.TimeUnit;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;
/**
 * 限流器 acquire 操作的 Observation 上下文。
 */

@Getter
@Setter
public class RRateLimiterAcquireContext extends Observation.Context {
    /**
     * 限流器名称
     */
    private final String rateLimiterName;
    /**
     * 当前线程名
     */
    private final String threadName;
    /**
     * 请求的许可数
     */
    private final long permits;
    /**
     * 等待超时时间
     */
    private final long timeout;
    /**
     * 超时时间单位
     */
    private final TimeUnit timeUnit;

    /**
     * 是否成功获取许可
     */
    private boolean rateLimiterAcquiredSuccessfully;

    /**
     * @param rateLimiterName 限流器名称
     * @param threadName 当前线程名
     * @param permits 请求的许可数
     * @param timeout 等待超时时间
     * @param timeUnit 超时时间单位
     */
    public RRateLimiterAcquireContext(String rateLimiterName, String threadName, long permits, long timeout, TimeUnit timeUnit) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.permits = permits;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }
}
