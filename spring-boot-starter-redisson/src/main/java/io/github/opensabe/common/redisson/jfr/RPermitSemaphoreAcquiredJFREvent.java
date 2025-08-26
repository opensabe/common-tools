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

import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreAcquiredContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "redisson", "rPermitSemaphore"})
@Label("acquire")
@StackTrace(false)
public class RPermitSemaphoreAcquiredJFREvent extends Event {
    @Label("permit semaphore name")
    private final String permitSemaphoreName;
    @Label("is try acquire")
    private final boolean tryAcquire;
    @Label("wait time")
    private final long waitTime;
    @Label("lease time")
    private final long leaseTime;
    @Label("unit")
    private final String unit;
    @Label("permit id")
    private String permitId;
    private String traceId;
    private String spanId;

    public RPermitSemaphoreAcquiredJFREvent(RPermitSemaphoreAcquiredContext rPermitSemaphoreAcquiredContext) {
        this.permitSemaphoreName = rPermitSemaphoreAcquiredContext.getSemaphoreName();
        this.tryAcquire = rPermitSemaphoreAcquiredContext.isTryAcquire();
        this.waitTime = rPermitSemaphoreAcquiredContext.getWaitTime();
        this.leaseTime = rPermitSemaphoreAcquiredContext.getLeaseTime();
        this.unit = rPermitSemaphoreAcquiredContext.getUnit() != null ? rPermitSemaphoreAcquiredContext.getUnit().name() : null;
    }
}
