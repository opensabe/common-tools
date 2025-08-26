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

import io.github.opensabe.common.redisson.observation.rsemaphore.RPermitSemaphoreReleasedContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Category({"observation", "redisson", "rPermitSemaphore"})
@Label("released")
@StackTrace(false)
public class RPermitSemaphoreReleasedJFREvent extends Event {
    @Label("permit semaphore name")
    private final String permitSemaphoreName;
    @Label("permit id")
    private final String permitId;
    @Label("is permit released successfully")
    private boolean permitReleasedSuccessfully;
    private String traceId;
    private String spanId;

    public RPermitSemaphoreReleasedJFREvent(RPermitSemaphoreReleasedContext rPermitSemaphoreReleasedContext) {
        this.permitSemaphoreName = rPermitSemaphoreReleasedContext.getSemaphoreName();
        this.permitId = rPermitSemaphoreReleasedContext.getPermitId();
    }
}
