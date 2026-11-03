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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SemicolonPathGuard")
class SemicolonPathGuardTest {

    @Test
    @DisplayName("rejects path with semicolon")
    void pathWithSemicolon() {
        assertTrue(SemicolonPathGuard.containsSemicolonInPath("/api;x=/"));
        assertTrue(SemicolonPathGuard.containsSemicolonInPath("/admin;bypass=1/users"));
        assertTrue(SemicolonPathGuard.containsSemicolonInPath("/a;b?c=1"));
    }

    @Test
    @DisplayName("allows normal path and semicolon only in query")
    void allowsNormalAndQuerySemicolon() {
        assertFalse(SemicolonPathGuard.containsSemicolonInPath("/api/users"));
        assertFalse(SemicolonPathGuard.containsSemicolonInPath("/api?a=1;2"));
        assertFalse(SemicolonPathGuard.containsSemicolonInPath("/api?x=1&y=2"));
        assertFalse(SemicolonPathGuard.containsSemicolonInPath(null));
        assertFalse(SemicolonPathGuard.containsSemicolonInPath(""));
    }
}
