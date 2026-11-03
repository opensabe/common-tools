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

/**
 * Detects semicolon (matrix-variable style) content in an HTTP path.
 * Query string is ignored — only the path segment is inspected.
 */
public final class SemicolonPathGuard {

    private SemicolonPathGuard() {
    }

    /**
     * @param requestUriOrPath full request URI (may include query) or a raw path
     * @return true if the path part contains {@code ;}
     */
    public static boolean containsSemicolonInPath(String requestUriOrPath) {
        if (requestUriOrPath == null || requestUriOrPath.isEmpty()) {
            return false;
        }
        String path = requestUriOrPath;
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        int fragment = path.indexOf('#');
        if (fragment >= 0) {
            path = path.substring(0, fragment);
        }
        return path.indexOf(';') >= 0;
    }
}
