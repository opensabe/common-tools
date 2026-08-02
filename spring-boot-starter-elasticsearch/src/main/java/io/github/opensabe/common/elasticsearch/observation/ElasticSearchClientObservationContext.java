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
package io.github.opensabe.common.elasticsearch.observation;

import io.micrometer.observation.Observation;
import lombok.Getter;
import lombok.Setter;

/**
 * Elasticsearch 客户端单次请求的 Observation 上下文。
 */
@Getter
@Setter
public class ElasticSearchClientObservationContext extends Observation.Context {
/** uri。 */
    private final String uri;
/** params。 */
    private final String params;

    private String response = "";
/** throwable。 */
    private Throwable throwable;

    public ElasticSearchClientObservationContext(String uri, String params) {
        this.uri = uri == null ? "" : uri;
        this.params = params == null ? "" : params;
    }
}
