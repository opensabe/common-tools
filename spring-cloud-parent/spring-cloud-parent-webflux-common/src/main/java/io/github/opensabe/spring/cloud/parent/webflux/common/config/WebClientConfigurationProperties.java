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
package io.github.opensabe.spring.cloud.parent.webflux.common.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "webclient")
public class WebClientConfigurationProperties {
    private Map<String, WebClientProperties> configs;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    @Data
    @NoArgsConstructor
    public static class WebClientProperties {
        private static AntPathMatcher antPathMatcher = new AntPathMatcher();
        private Cache<String, Boolean> retryablePathsMatchResult = Caffeine.newBuilder().build();
        /**
         * 服务地址，不填写则为 http://serviceName
         */
        private String baseUrl;
        /**
         * 微服务名称，不填写就是 configs 这个 map 的 key
         */
        private String serviceName;
        /**
         * 可以重试的路径，默认只对 GET 方法重试，通过这个配置增加针对某些非 GET 方法的路径的重试
         */
        private List<String> retryablePaths;
        /**
         * 最大连接数量
         */
        private int maxConnection = 50;
        /**
         * 连接超时
         */
        private Duration connectTimeout = Duration.ofMillis(500);
        /**
         * 响应超时
         */
        private Duration responseTimeout = Duration.ofSeconds(8);

        /**
         * 是否匹配
         *
         * @param path
         * @return
         */
        public boolean retryablePathsMatch(String path) {
            if (CollectionUtils.isEmpty(retryablePaths)) {
                return false;
            }
            return retryablePathsMatchResult.get(path, k -> {
                return retryablePaths.stream().anyMatch(pattern -> antPathMatcher.match(pattern, path));
            });
        }
    }
}
