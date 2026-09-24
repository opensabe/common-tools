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
package io.github.opensabe.common.elasticsearch.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "spring.data.elasticsearch")
@Data
public class ElasticSearchProperties {
    private String addresses;
    private Boolean secure = false;
    /**
     * RestClient HTTP 连接池与超时。未配置的字段保持 null，使用 ES RestClient 7.17.8 默认值。
     */
    private Client client = new Client();

    @Data
    public static class Client {
        /**
         * 整个客户端连接池最大连接总数。
         * 未配置时有效默认：30（{@code RestClientBuilder.DEFAULT_MAX_CONN_TOTAL}）。
         */
        private Integer maxConnTotal;

        /**
         * 每个路由（通常对应单个 ES 节点 host:port）的最大连接数。
         * 未配置时有效默认：10（{@code RestClientBuilder.DEFAULT_MAX_CONN_PER_ROUTE}）。
         */
        private Integer maxConnPerRoute;

        /**
         * 与 ES 节点建立 TCP 连接的超时。
         * 未配置时有效默认：1s（{@code RestClientBuilder.DEFAULT_CONNECT_TIMEOUT_MILLIS}）。
         */
        private Duration connectTimeout;

        /**
         * 连接建立后等待数据包的超时（含查询/写入耗时）。
         * 未配置时有效默认：30s（{@code RestClientBuilder.DEFAULT_SOCKET_TIMEOUT_MILLIS}）。
         */
        private Duration socketTimeout;

        /**
         * 从连接池借用连接的等待超时；池耗尽时超过此时长会失败。
         * 未配置时有效默认：无限等待（RestClient 未设置；Apache {@code RequestConfig} 为 -1）。
         */
        private Duration connectionRequestTimeout;
    }
}
