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

import io.github.opensabe.common.redisson.observation.rexpirable.RExpirableExpireContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Category({"observation", "redisson", "rexpirable"})
@Label("expire")
@StackTrace(false)
public class RExpirableExpireJFREvent extends Event {
    @Label("expirable name")
    private final String expirableName;
    @Label("expire")
    private final String expire;
    @Label("is expire set successfully")
    private boolean expireSetSuccessfully;
    private String traceId;
    private String spanId;

    public RExpirableExpireJFREvent(RExpirableExpireContext rExpirableExpireContext) {
        this.expirableName = rExpirableExpireContext.getExpirableName();
        this.expire = rExpirableExpireContext.getExpire();
    }
}
