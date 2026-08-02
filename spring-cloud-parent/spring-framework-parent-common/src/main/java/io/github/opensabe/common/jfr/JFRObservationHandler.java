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
package io.github.opensabe.common.jfr;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.log4j.Log4j2;


/**
 * 全局 {@link ObservationHandler}：将 Micrometer Observation 生命周期事件分派给 {@link ObservationToJFRGenerator}。
 * <p>
 * 使用方式：实现 {@link ObservationToJFRGenerator} 并注册为 Bean 即可。
 *
 * @see ObservationHandler
 */
@Log4j2
public class JFRObservationHandler<T extends Observation.Context> implements ObservationHandler<T> {

    /** 按上下文类型分组的 JFR 生成器列表。 */
    private final Map<Class<? extends Observation.Context>, List<ObservationToJFRGenerator<? extends Observation.Context>>> generatorMap;

    /**
     * @param generators 容器中所有 {@link ObservationToJFRGenerator} Bean
     */
    public JFRObservationHandler(List<ObservationToJFRGenerator<T>> generators) {
        this.generatorMap = generators.stream().collect(
                Collectors.groupingBy(ObservationToJFRGenerator::getContextClazz)
        );
    }

    /**
     * Observation 启动时调用匹配的生成器 {@link ObservationToJFRGenerator#onStart(Observation.Context)}。
     *
     * @param context 当前上下文
     */
    @Override
    public void onStart(Observation.Context context) {
        List<ObservationToJFRGenerator<? extends Observation.Context>> observationToJFRGenerators = generatorMap.get(context.getClass());
        if (observationToJFRGenerators != null) {
            log.debug("JFRObservationHandler-onStart {} -> observationToJFRGenerators {}", context.getName(), observationToJFRGenerators);
            observationToJFRGenerators.forEach(generator -> {
                try {
                    generator.onStart(context);
                } catch (Exception e) {
                    log.error("JFRObservationHandler-onStart error {}", e.getMessage(), e);
                }
            });
        }
    }

    /**
     * Observation 停止时调用匹配的生成器 {@link ObservationToJFRGenerator#onStop(Observation.Context)}。
     *
     * @param context 当前上下文
     */
    @Override
    public void onStop(Observation.Context context) {
        List<ObservationToJFRGenerator<? extends Observation.Context>> observationToJFRGenerators = generatorMap.get(context.getClass());
        if (observationToJFRGenerators != null) {
            log.debug("JFRObservationHandler-onStop {} -> observationToJFRGenerators {}", context.getName(), observationToJFRGenerators);
            observationToJFRGenerators.forEach(generator -> {
                try {
                    generator.onStop(context);
                } catch (Exception e) {
                    log.error("JFRObservationHandler-onStop error {}", e.getMessage(), e);
                }
            });
        }
    }

    /**
     * 接受所有 Observation 上下文（具体过滤由生成器 {@link ObservationToJFRGenerator#getContextClazz()} 完成）。
     *
     * @param context 当前上下文
     * @return 恒为 {@code true}
     */
    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }
}
