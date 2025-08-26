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

import io.github.opensabe.common.redisson.observation.rlock.RLockAcquiredContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Category({"observation", "redisson", "rlock"})
@Label("acquire lock")
@StackTrace(false)
public class RLockAcquiredJFREvent extends Event {
    @Label("lock name")
    private final String lockName;
    @Label("is try acquire")
    private final boolean tryAcquire;
    @Label("wait time")
    private final long waitTime;
    @Label("lease time")
    private final long leaseTime;
    @Label("time unit")
    private final String timeUnit;
    @Label("lock class")
    private final String lockClass;
    @Label("is lock acquired successfully")
    private boolean lockAcquiredSuccessfully;
    private String traceId;
    private String spanId;

    public RLockAcquiredJFREvent(RLockAcquiredContext rLockAcquiredContext) {
        this.lockName = rLockAcquiredContext.getLockName();
        this.tryAcquire = rLockAcquiredContext.isTryAcquire();
        this.waitTime = rLockAcquiredContext.getWaitTime();
        this.leaseTime = rLockAcquiredContext.getLeaseTime();
        this.timeUnit = rLockAcquiredContext.getTimeUnit() != null ? rLockAcquiredContext.getTimeUnit().name() : null;
        this.lockClass = rLockAcquiredContext.getLockClass().getSimpleName();
    }
}
