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

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class RRateLimiterAcquireContext extends Observation.Context {
    private final String rateLimiterName;
    private final String threadName;
    private final long permits;
    private final long timeout;
    private final TimeUnit timeUnit;

    private boolean rateLimiterAcquiredSuccessfully;

    public RRateLimiterAcquireContext(String rateLimiterName, String threadName, long permits, long timeout, TimeUnit timeUnit) {
        this.rateLimiterName = rateLimiterName;
        this.threadName = threadName;
        this.permits = permits;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }
}
