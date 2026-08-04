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
package io.github.opensabe.common.config;

import io.github.opensabe.common.utils.json.JsonUtil;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.json.JsonMapper;

/**
 * 将 Boot 容器中的 {@link JsonMapper} 桥接到 {@link JsonUtil} 静态门面。
 * <p>
 * 显式表达「Spring mapper 劫持」副作用，避免把 {@link JsonUtil} 本身伪装成业务 Bean。
 */
@Log4j2
public final class JsonUtilSpringBridge {

    /**
     * 用容器中的 {@link JsonMapper} 替换 {@link JsonUtil} 的静态 mapper。
     *
     * @param jsonMapper Boot 自动配置的 Jackson {@link JsonMapper}
     */
    public JsonUtilSpringBridge(JsonMapper jsonMapper) {
        JsonUtil.adopt(jsonMapper);
        log.info("JsonUtil adopted Spring JsonMapper bean: {}", jsonMapper.getClass().getName());
    }
}
