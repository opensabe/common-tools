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
package io.github.opensabe.common.redisson.observation.rexpirable;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;
/**
 * RExpirable 过期设置操作的 Observation 上下文。
 */

@Getter
@Setter
public class RExpirableExpireContext extends Observation.Context {
    /**
     * 可过期对象名称
     */
    private final String expirableName;
    /**
     * 当前线程名
     */
    private final String threadName;
    /**
     * 过期操作描述
     */
    private final String expire;

    /**
     * 是否成功设置过期
     */
    private boolean expireSetSuccessfully;

    public RExpirableExpireContext(String expirableName, String threadName, String expire) {
        this.expirableName = expirableName;
        this.threadName = threadName;
        this.expire = expire;
    }
}
