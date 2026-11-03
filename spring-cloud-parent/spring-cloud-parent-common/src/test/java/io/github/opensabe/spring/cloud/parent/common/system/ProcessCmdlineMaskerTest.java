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
package io.github.opensabe.spring.cloud.parent.common.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProcessCmdlineMasker")
class ProcessCmdlineMaskerTest {

    @Test
    @DisplayName("masks sensitive curl header and data flags")
    void masksCurlFlags() {
        String input = "curl -H Authorization: Bearer secret-token-value -d password=supersecret http://x";
        String masked = ProcessCmdlineMasker.mask(input);
        assertTrue(masked.contains("Authorization: "));
        assertFalse(masked.contains("secret-token-value"));
        assertFalse(masked.contains("supersecret"));
        assertTrue(masked.contains(ProcessCmdlineMasker.MASK));
    }

    @Test
    @DisplayName("masks sensitive URL query params")
    void masksUrlQuery() {
        String input = "wget https://example.com/api?token=abcdefg&id=1";
        String masked = ProcessCmdlineMasker.mask(input);
        assertFalse(masked.contains("abcdefg"));
        assertTrue(masked.contains("token=" + ProcessCmdlineMasker.MASK)
                || masked.contains("token=ab" + ProcessCmdlineMasker.MASK + "fg"));
        assertTrue(masked.contains("id=1"));
    }

    @Test
    @DisplayName("maskValue short and long forms")
    void maskValue() {
        assertEquals(ProcessCmdlineMasker.MASK, ProcessCmdlineMasker.maskValue("ab"));
        assertEquals("ab" + ProcessCmdlineMasker.MASK + "yz", ProcessCmdlineMasker.maskValue("ab12yz"));
    }
}
