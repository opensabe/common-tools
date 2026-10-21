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

package io.github.opensabe.common.internalcall;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalCallFailureHandlerTest {

    @Test
    @DisplayName("去重：同一 fingerprint 第二次不抛")
    void dedup() {
        InternalCallProperties props = new InternalCallProperties();
        props.getFailure().setAlarmDedupSeconds(60);
        InternalCallFailureHandler handler = new InternalCallFailureHandler(props);
        assertDoesNotThrow(() -> handler.report(
                InternalCallFailureReason.MISSING_HEADER, "10.0.0.1", "/api", "gw"));
        assertDoesNotThrow(() -> handler.report(
                InternalCallFailureReason.MISSING_HEADER, "10.0.0.1", "/api", "gw"));
    }

    @Test
    @DisplayName("path 截断到 256")
    void truncate() {
        String longPath = "x".repeat(300);
        assertEquals(256, InternalCallFailureHandler.truncate(longPath, 256).length());
    }

    @Test
    @DisplayName("path 去掉 query，避免日志泄露")
    void stripQuery() {
        assertEquals("/api/order", InternalCallFailureHandler.stripQuery("/api/order?token=secret"));
        assertEquals("", InternalCallFailureHandler.stripQuery(null));
    }
}
