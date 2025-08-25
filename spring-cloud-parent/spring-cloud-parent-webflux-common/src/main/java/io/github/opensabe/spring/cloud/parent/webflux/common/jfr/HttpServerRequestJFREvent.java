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
package io.github.opensabe.spring.cloud.parent.webflux.common.jfr;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.StackTrace;
import lombok.Getter;
import lombok.Setter;

@Category({"observation", "reactive"})
@Label("Http Server Request")
@StackTrace(false)
@Getter
@Setter
public class HttpServerRequestJFREvent extends Event {
    @Label("httpMethod")
    @Description("the httpMethod of the request")
    private final String httpMethod;

    @Label("uri")
    @Description("the uri of the request")
    private final String uri;

    @Label("queryString")
    @Description("the queryString of the request")
    private final String queryString;

    @Label("requestHeaders")
    @Description("the headers of the request")
    private final String requestHeaders;

    @Label("throwable")
    @Description("the error of the request")
    private String throwable;

    @Label("traceId")
    @Description("the traceId of the request")
    private String traceId;

    @Label("spanId")
    @Description("the spanId of the request")
    private String spanId;

    @Label("status")
    @Description("the status of the response")
    private int status;

    @Label("responseHeaders")
    @Description("the headers of the response")
    private String responseHeaders;

    public HttpServerRequestJFREvent(String httpMethod, String uri, String queryString, String requestHeaders) {
        this.httpMethod = httpMethod;
        this.uri = uri;
        this.queryString = queryString;
        this.requestHeaders = requestHeaders;
    }
}
