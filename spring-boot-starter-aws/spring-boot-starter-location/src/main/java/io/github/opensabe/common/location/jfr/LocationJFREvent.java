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
package io.github.opensabe.common.location.jfr;

import io.github.opensabe.common.location.observation.LocationContext;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

/**
 * Location JFR 事件。
 */
@Setter
@Getter
@Category({"observation", "location"})
@Label("geocode")
@StackTrace(false)
public class LocationJFREvent extends Event {
    // 方法名称
    @Label("method name")
/** methodName。 */
    private String methodName;

    // 请求参数
    @Label("request params")
/** requestParams。 */
    private String requestParams;

    // 响应结果
    @Label("response")
/** response。 */
    private String response;

    // 执行时间（毫秒）
    @Label("execution time")
/** executionTime。 */
    private long executionTime;

    @Label("successful")
/** successful。 */
    private boolean successful;

    @Label("trace id")
/** traceId。 */
    private String traceId;

    @Label("span id")
/** spanId。 */
    private String spanId;

    // 默认构造器
    public LocationJFREvent() {
    }

    // 通过 LocationContext 初始化
    public LocationJFREvent(LocationContext locationContext) {
        this.methodName = locationContext.getMethodName();
        this.requestParams = locationContext.getRequestParams() != null
                ? locationContext.getRequestParams().toString()
                : null;
        this.response = locationContext.getResponse() != null
                ? locationContext.getResponse().toString()
                : null;
        this.executionTime = locationContext.getExecutionTime();
    }
}