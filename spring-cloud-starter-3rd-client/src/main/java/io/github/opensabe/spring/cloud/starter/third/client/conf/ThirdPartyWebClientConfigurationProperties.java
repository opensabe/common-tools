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
package io.github.opensabe.spring.cloud.starter.third.client.conf;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "third-party.webclient")
public class ThirdPartyWebClientConfigurationProperties {
    private Map<String, WebClientProperties> configs;

    @Data
    @NoArgsConstructor
    public static class WebClientProperties {
        /**
         * 服务地址，必须填写
         */
        private String baseUrl;
        /**
         * 服务名称，不填写就是 configs 这个 map 的 key，目前没啥用
         */
        private String serviceName;
        /**
         * 连接超时
         */
        private Duration connectTimeout = Duration.ofSeconds(3);
        /**
         * 响应超时
         */
        private Duration responseTimeout = Duration.ofSeconds(15);
    }
}
