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

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;
/**
 * 信号量许可释放操作的 Observation 上下文。
 */

@Setter
@Getter
public class RPermitSemaphoreReleasedContext extends Observation.Context {
    /**
     * 信号量名称
     */
    private final String semaphoreName;
    /**
     * 当前线程名
     */
    private final String threadName;
    /**
     * 释放的许可 ID
     */
    private final String permitId;
    /**
     * 是否释放成功
     */
    private boolean permitReleasedSuccessfully;

    public RPermitSemaphoreReleasedContext(String semaphoreName, String permitId) {
        this.semaphoreName = semaphoreName;
        this.threadName = Thread.currentThread().getName();
        this.permitId = permitId;
    }
}
