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
package io.github.opensabe.common.location.observation;

import io.micrometer.observation.Observation;
import lombok.*;

/**
 * @author changhongwei
 * @date 2025/1/21 16:06
 * @description:
 */
@Getter
@Setter
@NoArgsConstructor
public class LocationContext extends Observation.Context  {

    // 方法名称
    private String methodName;

    // 请求参数
    private Object requestParams;

    // 响应结果
    private Object response;

    // 执行时间（毫秒）
    private long executionTime;

    private boolean successful ;
    private Throwable throwable;

    public LocationContext(String methodName, Object requestParams, Object response, long executionTime,boolean successful, Throwable throwable) {
        this.methodName = methodName;
        this.requestParams = requestParams;
        this.response = response;
        this.executionTime = executionTime;
        this.throwable = throwable;
        this.successful=successful;
    }
}