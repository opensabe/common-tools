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
package io.github.opensabe.common.jackson;

import java.time.LocalDateTime;

import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson 模块：为 {@link LocalDateTime} 注册毫秒时间戳序列化/反序列化器。
 * <p>
 * long 与 {@link LocalDateTime} 互转时精度为毫秒，亚毫秒部分会被忽略；生成时间时建议
 * {@code LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)}。
 */
public class TimestampModule extends SimpleModule {

    /**
     * 注册 {@link TimestampLocalDateTimeSerializer} 与 {@link LongToLocalDateTimeDeserializer}。
     */
    public TimestampModule() {
        addSerializer(LocalDateTime.class, TimestampLocalDateTimeSerializer.getINSTANCE());
        addDeserializer(LocalDateTime.class, LongToLocalDateTimeDeserializer.getINSTANCE());
    }
}
