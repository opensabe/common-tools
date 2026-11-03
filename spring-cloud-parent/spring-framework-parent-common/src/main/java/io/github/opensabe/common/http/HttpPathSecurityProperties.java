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
package io.github.opensabe.common.http;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP path security: reject matrix-variable style semicolons in the URI path.
 * Defaults favor security; legitimate paths are not expected to contain {@code ;}.
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "opensabe.http")
public class HttpPathSecurityProperties {

    private RejectSemicolon rejectSemicolon = new RejectSemicolon();

    /**
     * When true, MVC UrlPathHelper keeps semicolon content (no strip before match).
     * Important under ant_path_matcher; harmless under PathPattern.
     */
    private boolean keepSemicolonInPath = true;

    @Data
    @NoArgsConstructor
    public static class RejectSemicolon {
        /**
         * Reject requests whose path contains {@code ;}. Default true.
         */
        private boolean enabled = true;
    }
}
