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
package io.github.opensabe.spring.cloud.starter.third.client.test;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HttpBin {@code /anything} 端点响应体（测试用）。
 */
@NoArgsConstructor
@Data
public class HttpBinAnythingResponse {

    /** 查询参数。 */
    private Map<String, List<String>> args;

    /** 原始请求体。 */
    private String data;

    /** 表单字段。 */
    private Map<String, List<String>> form;

    /** 请求头。 */
    private Map<String, List<String>> headers;

    /** HTTP 方法。 */
    private String method;

    /** 客户端来源地址。 */
    private String origin;

    /** 完整请求 URL。 */
    private String url;
}
