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

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * 时间戳序列化模块
 */
public class TimestampModule extends SimpleModule {

    public TimestampModule() {
        //LocalDateTime 与 Long 序列化，反序列化，会转换为毫秒时间戳，毫秒以下的时间戳会被忽略
        //所以，LocalDateTime 最好在生成的时候 truncate 到毫秒，比如：
        //LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        addSerializer(LocalDateTime.class, TimestampLocalDateTimeSerializer.getINSTANCE());
        addDeserializer(LocalDateTime.class, LongToLocalDateTimeDeserializer.getINSTANCE());
    }
}
