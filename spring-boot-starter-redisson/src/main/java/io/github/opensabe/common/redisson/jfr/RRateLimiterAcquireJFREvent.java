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
package io.github.opensabe.common.redisson.jfr;

import io.github.opensabe.common.redisson.observation.ratelimiter.RRateLimiterAcquireContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "redisson", "rRateLimiter"})
@Label("acquire")
@StackTrace(false)
public class RRateLimiterAcquireJFREvent extends Event {
    @Label("rate limiter name")
    private final String rateLimiterName;
    @Label("permits")
    private final long permits;
    @Label("timeout")
    private final long timeout;
    @Label("time unit")
    private final String timeUnit;
    @Label("is acquire successfully")
    private boolean acquireSuccessfully;
    private String traceId;
    private String spanId;

    public RRateLimiterAcquireJFREvent(RRateLimiterAcquireContext rRateLimiterAcquireContext) {
        this.rateLimiterName = rRateLimiterAcquireContext.getRateLimiterName();
        this.permits = rRateLimiterAcquireContext.getPermits();
        this.timeout = rRateLimiterAcquireContext.getTimeout();
        this.timeUnit = rRateLimiterAcquireContext.getTimeUnit() != null ? rRateLimiterAcquireContext.getTimeUnit().name() : null;
    }
}
