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

import java.time.Duration;
import java.util.Objects;

import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;
/**
 * 限流器 setRate/updateRate 操作的 Observation 上下文。
 */

@Getter
@Setter
public class RRateLimiterSetRateContext extends Observation.Context {
    /**
     * 限流器名称
     */
    private final String rateLimiterName;
    /**
     * 当前线程名
     */
    private final String threadName;
    private final RateType mode;
    /**
     * 设置的速率
     */
    private final long rate;
    /**
     * 速率时间窗口
     */
    private final long rateInterval;
    private final RateIntervalUnit rateIntervalUnit;
    private final long keepAlive;
    /**
     * 是否设置成功
     */
    private boolean setRateSuccessfully;

    public RRateLimiterSetRateContext(String rateLimiterName, String threadName, RateType mode, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.mode = mode;
        this.rate = rate;
        this.rateInterval = rateInterval;
        this.rateIntervalUnit = rateIntervalUnit;
        this.keepAlive = Duration.ZERO.toMillis();
    }

    public RRateLimiterSetRateContext(String rateLimiterName, String threadName, RateType mode, long rate, Duration rateInterval, Duration keepAlive) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.mode = mode;
        this.rate = rate;
        if (Objects.isNull(rateInterval)) {
            this.rateInterval = Duration.ZERO.toMillis();
        } else {
            this.rateInterval = rateInterval.toMillis();
        }
        this.rateIntervalUnit = RateIntervalUnit.MILLISECONDS;
        if (Objects.isNull(keepAlive)) {
            this.keepAlive = Duration.ZERO.toMillis();
        } else {
            this.keepAlive = keepAlive.toMillis();
        }
    }
}
