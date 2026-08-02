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
 * 信号量许可数变更操作的 Observation 上下文。
 */

@Getter
@Setter
public class RPermitSemaphoreModifiedContext extends Observation.Context {
    /**
     * 信号量名称
     */
    private final String semaphoreName;
    /**
     * 当前线程名
     */
    private final String threadName;
    /**
     * 变更操作描述
     */
    private final String modified;

    /**
     * 是否变更成功
     */
    private boolean modifiedSuccessfully;

    public RPermitSemaphoreModifiedContext(String semaphoreName, String modified) {
        this.semaphoreName = semaphoreName;
        this.threadName = Thread.currentThread().getName();
        this.modified = modified;
    }
}
