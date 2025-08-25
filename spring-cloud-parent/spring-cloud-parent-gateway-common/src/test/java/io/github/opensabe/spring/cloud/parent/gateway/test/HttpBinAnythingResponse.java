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
package io.github.opensabe.spring.cloud.parent.gateway.test;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class HttpBinAnythingResponse {
    private Map<String, List<String>> args;
    private String data;
    private Map<String, List<String>> form;
    private Map<String, List<String>> headers;
    private String method;
    private String origin;
    private String url;
}
