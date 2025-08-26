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
package io.github.opensabe.common.redisson.observation.rlock;

import java.util.concurrent.TimeUnit;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RLockAcquiredContext extends Observation.Context {

    private final String lockName;
    /**
     * 是否是 try 获取
     */
    private final boolean tryAcquire;
    private final long waitTime;
    private final long leaseTime;
    private final TimeUnit timeUnit;
    private final Class lockClass;
    private final String threadName;

    /**
     * 是否成功获取到了锁
     */
    private boolean lockAcquiredSuccessfully = false;

    public RLockAcquiredContext(String lockName, boolean tryAcquire, long waitTime, long leaseTime, TimeUnit timeUnit, Class lockClass) {
        this.lockName = lockName;
        this.tryAcquire = tryAcquire;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
        this.timeUnit = timeUnit;
        this.lockClass = lockClass;
        this.threadName = Thread.currentThread().getName();
    }
}
