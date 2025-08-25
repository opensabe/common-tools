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

import java.util.Objects;
import io.github.opensabe.common.jfr.ObservationToJFRGenerator;
import io.github.opensabe.common.location.observation.LocationContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;
import lombok.extern.log4j.Log4j2;

/**
 * @author changhongwei
 * @date 2025/1/22 10:26
 * @description: 负责在 LocationContext 的观测过程中创建和提交 LocationJFREvent。
 */

@Log4j2
public class LocationObservationToJFRGenerator extends ObservationToJFRGenerator<LocationContext> {

    @Override
    public Class<LocationContext> getContextClazz() {
        return LocationContext.class;
    }

    @Override
    protected boolean shouldCommitOnStop(LocationContext context) {
        // 判断是否需要在 Observation 停止时提交 JFR 事件
        return context.containsKey(LocationJFREvent.class);
    }

    @Override
    protected boolean shouldGenerateOnStart(LocationContext context) {
        // 始终生成 JFR 事件
        return true;
    }

    @Override
    protected void commitOnStop(LocationContext context) {
        // 从上下文中获取 JFR 事件
        LocationJFREvent locationJFREvent = context.get(LocationJFREvent.class);
        if (locationJFREvent == null) {
            log.warn("LocationJFREvent not found in context, skipping commit.");
            return;
        }

        // 设置 Trace 信息（如果存在）
        TracingObservationHandler.TracingContext tracingContext = context.get(TracingObservationHandler.TracingContext.class);
        if (Objects.nonNull(tracingContext)) {
            if (Objects.nonNull(tracingContext.getSpan())) {
                TraceContext traceContext = tracingContext.getSpan().context();
                locationJFREvent.setTraceId(traceContext.traceId());
                locationJFREvent.setSpanId(traceContext.spanId());
            } else {
                log.warn("Span is null in tracing context, skipping trace ID and span ID population.");
            }
        } else {
            log.warn("Tracing context is null, skipping trace ID and span ID population.");
        }

        // 设置事件字段
        locationJFREvent.setSuccessful(context.isSuccessful());
        locationJFREvent.setExecutionTime(context.getExecutionTime());
        locationJFREvent.setResponse(String.valueOf(context.getResponse()));
        locationJFREvent.setMethodName(context.getMethodName());
        locationJFREvent.setRequestParams(String.valueOf(context.getRequestParams()));

        // 提交 JFR 事件
        locationJFREvent.commit();
    }

    @Override
    protected void generateOnStart(LocationContext context) {
        // 创建新的 LocationJFREvent
        LocationJFREvent jfrEvent = new LocationJFREvent(context);

        // 开始 JFR 事件
        jfrEvent.begin();

        // 将事件存入上下文
        context.put(LocationJFREvent.class, jfrEvent);
    }
}