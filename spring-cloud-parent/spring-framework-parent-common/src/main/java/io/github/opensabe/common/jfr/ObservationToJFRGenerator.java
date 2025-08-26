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

import io.micrometer.observation.Observation;
import lombok.extern.log4j.Log4j2;

/**
 * 使用 Observation 生成 JFR 事件
 * 实现类的 Bean 会自动被 JFRObservationHandler 注册
 */
@Log4j2
public abstract class ObservationToJFRGenerator<T extends Observation.Context> {

    /**
     * 匹配 Context 类型
     *
     * @return
     */
    public abstract Class<T> getContextClazz();

    /**
     * @param context
     * @see JFRObservationHandler#onStop(Observation.Context)
     */
    public void onStop(Observation.Context context) {
        T cast = getContextClazz().cast(context);
        if (shouldCommitOnStop(cast)) {
            commitOnStop(cast);
        }
    }

    /**
     * @param context
     * @see JFRObservationHandler#onStart(Observation.Context)
     */
    public void onStart(Observation.Context context) {
        T cast = getContextClazz().cast(context);
        if (shouldGenerateOnStart(cast)) {
            generateOnStart(cast);
        }
    }

    /**
     * 是否在 stop 的时候 commit JFR 事件
     *
     * @param context
     * @return
     */
    protected abstract boolean shouldCommitOnStop(T context);

    /**
     * 是否在 start 的时候生成 JFR 事件
     *
     * @param context
     * @return
     */
    protected abstract boolean shouldGenerateOnStart(T context);

    /**
     * 在 stop 的时候 commit JFR 事件
     *
     * @param context
     * @return
     */
    protected abstract void commitOnStop(T context);

    /**
     * 在 start 的时候生成 JFR 事件
     *
     * @param context
     * @return
     */
    protected abstract void generateOnStart(T context);
}
