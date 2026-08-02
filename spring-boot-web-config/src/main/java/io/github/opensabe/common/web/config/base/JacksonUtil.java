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
package io.github.opensabe.common.web.config.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * HTTP 边界 Jackson 3 工具类。
 * <p>
 * 创建独立 {@link ObjectMapper}：忽略未知属性、序列化时省略 null 字段。
 * 与 {@link io.github.opensabe.common.utils.json.JsonUtil} 及 Fastjson 路径分离，勿假定全域一致。
 */
public class JacksonUtil {

    /**
     * 创建用于 Web 错误响应等场景的 Jackson 3 {@link ObjectMapper}。
     *
     * @return 配置好的 ObjectMapper
     */
    public static ObjectMapper createMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }
}
