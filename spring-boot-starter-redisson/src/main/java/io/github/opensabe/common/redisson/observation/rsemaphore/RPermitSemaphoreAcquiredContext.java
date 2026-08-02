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
package io.github.opensabe.common.redisson.observation.rsemaphore;

import java.util.concurrent.TimeUnit;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;
/**
 * 可过期信号量许可获取操作的 Observation 上下文。
 */

@Getter
@Setter
public class RPermitSemaphoreAcquiredContext extends Observation.Context {
    /**
     * 信号量名称
     */
    private final String semaphoreName;
    /**
     * 当前线程名
     */
    private final String threadName;
    /**
     * 是否为 tryAcquire 路径
     */
    private final boolean tryAcquire;
    /**
     * 等待时间
     */
    private final long waitTime;
    /**
     * 许可租约时间
     */
    private final long leaseTime;
    /**
     * 时间单位
     */
    private final TimeUnit unit;
    /**
     * 获取到的许可 ID
     */
    private String permitId;

    public RPermitSemaphoreAcquiredContext(String semaphoreName, boolean tryAcquire, long waitTime, long leaseTime, TimeUnit unit) {
        this.semaphoreName = semaphoreName;
        this.threadName = Thread.currentThread().getName();
        this.tryAcquire = tryAcquire;
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
        this.unit = unit;
    }
}
