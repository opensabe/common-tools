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
 * 将 Micrometer {@link Observation.Context} 转为 JFR 事件的抽象生成器。
 * <p>
 * 实现类注册为 Spring Bean 后由 {@link JFRObservationHandler} 按 {@link #getContextClazz()} 路由调用。
 */
@Log4j2
public abstract class ObservationToJFRGenerator<T extends Observation.Context> {

    /**
     * 本生成器处理的 Observation 上下文类型。
     *
     * @return 上下文 Class
     */
    public abstract Class<T> getContextClazz();

    /**
     * Observation 停止时分派至 {@link #commitOnStop(Observation.Context)}。
     *
     * @param context 当前 Observation 上下文
     * @see JFRObservationHandler#onStop(Observation.Context)
     */
    public void onStop(Observation.Context context) {
        T cast = getContextClazz().cast(context);
        if (shouldCommitOnStop(cast)) {
            commitOnStop(cast);
        }
    }

    /**
     * Observation 启动时分派至 {@link #generateOnStart(Observation.Context)}。
     *
     * @param context 当前 Observation 上下文
     * @see JFRObservationHandler#onStart(Observation.Context)
     */
    public void onStart(Observation.Context context) {
        T cast = getContextClazz().cast(context);
        if (shouldGenerateOnStart(cast)) {
            generateOnStart(cast);
        }
    }

    /**
     * 是否在 stop 时 commit JFR 事件。
     *
     * @param context 强类型上下文
     * @return {@code true} 表示应 commit
     */
    protected abstract boolean shouldCommitOnStop(T context);

    /**
     * 是否在 start 时生成 JFR 事件。
     *
     * @param context 强类型上下文
     * @return {@code true} 表示应生成
     */
    protected abstract boolean shouldGenerateOnStart(T context);

    /**
     * 在 stop 时 commit JFR 事件。
     *
     * @param context 强类型上下文
     */
    protected abstract void commitOnStop(T context);

    /**
     * 在 start 时生成 JFR 事件。
     *
     * @param context 强类型上下文
     */
    protected abstract void generateOnStart(T context);
}
