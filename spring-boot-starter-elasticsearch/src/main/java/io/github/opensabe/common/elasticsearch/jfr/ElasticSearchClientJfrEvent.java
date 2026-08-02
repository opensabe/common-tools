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
package io.github.opensabe.common.elasticsearch.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

/**
 * Elasticsearch 客户端 HTTP 请求的 JFR 事件。
 */
@Getter
@Setter
@Category({"ElasticSearch"})
@Label("Client Request")
@StackTrace(value = false)
public class ElasticSearchClientJfrEvent extends Event {
/** uri。 */
    private final String uri;
/** params。 */
    private final String params;

/** traceId。 */
    private String traceId;
/** spanId。 */
    private String spanId;

/** response。 */
    private String response;
/** throwable。 */
    private Throwable throwable;

    public ElasticSearchClientJfrEvent(String uri, String params) {
        this.uri = uri;
        this.params = params;
    }
}
