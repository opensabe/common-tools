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
package io.github.opensabe.common.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.handler.TracingObservationHandler;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * 如果直接注入 ObservationRegistry，可能会导致 ObservationAutoConfiguration 的
 * ObservationRegistryPostProcessor 与 ObservationRegistry 的初始化顺序颠倒
 * 如果先初始化了 ObservationRegistry，那么 ObservationRegistryPostProcessor 就会不生效，导致全局没有 Observation
 * 即没有链路也没有监控指标
 * 所以这里使用一个代理类，延迟初始化 ObservationRegistry，保证 ObservationRegistryPostProcessor 的生效
 */
@Log4j2
public class UnifiedObservationFactory {
    private final ObjectProvider<ObservationRegistry> objectProvider;
    private ObservationRegistry observationRegistry;

    private final static VarHandle OBSERVATION_REGISTRY_HANDLE;

    /**
     * 通过反射获取 TRACE_PARENT 的 key，用于获取各种追踪传递信息
     * 例如 http 调用放入 header 的链路信息
     * 主要用于单元测试
     * 参考代码：io.micrometer.tracing.brave.bridge.W3CPropagation
     * TraceContext.Injector<R> injector(Setter<R, String> setter)
     */
    public static final String TRACE_PARENT = "traceparent";
    public static final char TRACEPARENT_DELIMITER = '-';

    static {
        try {
            OBSERVATION_REGISTRY_HANDLE = MethodHandles.privateLookupIn(UnifiedObservationFactory.class, MethodHandles.lookup())
                    .findVarHandle(UnifiedObservationFactory.class, "observationRegistry", ObservationRegistry.class);
        } catch (Exception e) {
            throw new Error(e);
        }
    }



    public UnifiedObservationFactory(ObjectProvider<ObservationRegistry> objectProvider) {
        this.objectProvider = objectProvider;
    }

    /**
     * 使用 VarHandle 的 release/acquire 内存屏障，保证 observationRegistry 的可见性
     * 比使用 volatile 更轻量级，参考：https://zhuanlan.zhihu.com/p/499524262
     * @return
     */
    public ObservationRegistry getObservationRegistry() {
        if (observationRegistry == null) {
            synchronized (this) {
                ObservationRegistry val = observationRegistry;
                if (val == null) {
                    val = objectProvider.getIfAvailable();
                    OBSERVATION_REGISTRY_HANDLE.setRelease(this, val);
                }
                return val;
            }
        }
        return observationRegistry;
    }

    /**
     * 获取当前的 Observation
     */
    @Nullable
    public Observation getCurrentObservation() {
        return getObservationRegistry().getCurrentObservation();
    }

    /**
     * 创建新的Observation, task center,mq consumer等场景不需要继承parent，需要新起一个
     * @return
     */
    public Observation createEmptyObservation () {
        return DefaultEmptyObservationDocumentation.EMPTY_OBSERVATION_DOCUMENTATION.start(getObservationRegistry());
    }
    /**
     * 获取当前的 Observation，如果没有则创建一个
     * 但是这个只是适用于本身不需要单独记录 Observation 的情况，而是直接复用当前的 Observation
     * 如果当前的 Observation 本身就是空的，那么就会创建一个空的 Observation，但是这个是异常情况
     * 如果你本身想单独记录一个 Observation，那么请使用标准的 ObservationConvention + ObservationContext + ObservationDocumentation 的方式
     * 可以参考：FeignContext，FeignObservationConvention，FeignObservationDocumentation
     * @return
     */
    @NotNull
    public Observation getCurrentOrCreateEmptyObservation() {
        Observation currentObservation = getCurrentObservation();
        if (currentObservation == null) {
            log.info("Current observation is null, create a empty observation");
            currentObservation = createEmptyObservation();
        }
        return currentObservation;
    }

    /**
     * 获取 Observation 的 TraceContext
     * 没有提供返回 Span 的方法，因为基本用不上，
     * 如果要保持链路不能像之前使用 Span 而是要用 Observation，所以不提供 Span 的方法，只提供了 Observation
     *
     * @param observation
     * @return
     */
    @Nullable
    public static TraceContext getTraceContext(Observation observation) {
        TracingObservationHandler.TracingContext tracingContext = observation.getContext().get(TracingObservationHandler.TracingContext.class);
        if (tracingContext == null) {
            return null;
        }
        Span span = tracingContext.getSpan();
        if (span == null) {
            return null;
        }
        return span.context();
    }
}
